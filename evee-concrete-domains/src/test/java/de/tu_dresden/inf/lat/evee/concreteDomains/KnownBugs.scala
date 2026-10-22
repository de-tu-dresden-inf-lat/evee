package de.tu_dresden.inf.lat.evee.concreteDomains

import java.io.File
import java.security.Permission

import org.junit.runner.{Description, JUnitCore, Request}
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.{Ignore, Test}

import scala.collection.JavaConverters._

/**
 * Runs the tests that are disabled because of a known bug, i.e., annotated with @Ignore("B<n>"), as if they were
 * enabled, and reports for each of them whether it still fails. A test that passes means that its bug is fixed and
 * its @Ignore has to be removed. Tests that call System.exit fail instead of ending the run.
 *
 * Usage, after `mvn test-compile`:
 * {{{
 * java -cp target/test-classes:target/classes:<test classpath> de.tu_dresden.inf.lat.evee.concreteDomains.KnownBugs [test class ...]
 * }}}
 * Without arguments, it checks all test classes in this package. The exit code is 1 if a disabled test passes.
 */
object KnownBugs {

  private case class Result(bug: String, test: String, passed: Boolean, failure: String)

  /** runs ignored tests like enabled ones */
  private class IgnoringRunner(testClass: Class[_]) extends BlockJUnit4ClassRunner(testClass) {
    override protected def isIgnored(method: FrameworkMethod): Boolean = false
  }

  /** turns System.exit into an exception, so that a test calling it fails instead of ending the run */
  private object NoExit extends SecurityManager {
    override def checkExit(status: Int): Unit = throw new SecurityException(s"System.exit($status) called")
    override def checkPermission(permission: Permission): Unit = ()
  }

  def main(args: Array[String]): Unit = {
    val classes = if (args.nonEmpty) args.toSeq.map(Class.forName) else testClassesInPackage
    try System.setSecurityManager(NoExit)
    catch {
      case e: UnsupportedOperationException => println("Warning: System.exit cannot be intercepted: " + e.getMessage)
    }

    val results = for {
      testClass <- classes
      method <- testClass.getMethods.toSeq.sortBy(_.getName)
      ignore = method.getAnnotation(classOf[Ignore])
      if method.getAnnotation(classOf[Test]) != null && ignore != null && ignore.value.startsWith("B")
    } yield run(testClass, method.getName, ignore.value)

    System.setSecurityManager(null)
    report(results)
    System.exit(if (results.exists(_.passed)) 1 else 0)
  }

  private def run(testClass: Class[_], method: String, bug: String): Result = {
    val request = Request.runner(new IgnoringRunner(testClass))
      .filterWith(Description.createTestDescription(testClass, method))
    val result = new JUnitCore().run(request)
    val failure = result.getFailures.asScala.headOption.map { f =>
      val e = rootCause(f.getException)
      e.getClass.getSimpleName + ": " + String.valueOf(e.getMessage).linesIterator.toSeq.headOption.getOrElse("")
    }.getOrElse("")
    Result(bug, testClass.getSimpleName + "." + method, result.wasSuccessful, failure)
  }

  private def rootCause(e: Throwable): Throwable = if (e.getCause == null || e.getCause == e) e else rootCause(e.getCause)

  private def report(results: Seq[Result]): Unit = {
    def firstBugNumber(bug: String) = "\\d+".r.findFirstIn(bug).map(_.toInt).getOrElse(0)
    for (r <- results.sortBy(r => (firstBugNumber(r.bug), r.test))) {
      val status = if (r.passed) "PASSES" else "fails "
      println(f"${r.bug}%-8s $status  ${r.test}" + (if (r.passed) "  <- remove its @Ignore" else "  (" + r.failure.take(100) + ")"))
    }
    val passing = results.count(_.passed)
    println(s"${results.size} disabled tests: ${results.size - passing} still fail, $passing pass")
  }

  private def testClassesInPackage: Seq[Class[_]] = {
    val packageName = getClass.getPackage.getName
    val directory = new File(getClass.getResource("/" + packageName.replace('.', '/')).toURI)
    directory.listFiles.toSeq.map(_.getName).filter(_.endsWith("Test.class")).sorted
      .map(file => Class.forName(packageName + "." + file.stripSuffix(".class")))
  }
}
