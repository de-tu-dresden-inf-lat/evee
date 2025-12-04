//general settings:
ThisBuild / name := "evee-elimination-proofs"
ThisBuild / organization := "de.tu-dresden.inf.lat.evee"
ThisBuild / version := "0.1.1-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.6"
ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / publishConfiguration := publishConfiguration.value.withOverwrite(true)
ThisBuild / publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
ThisBuild / excludeDependencies ++= Seq(
  // commons-logging is replaced by jcl-over-slf4j
  ExclusionRule("commons-logging", "commons-logging"),
  // axiom-impl replaced by axiom-dom
  ExclusionRule("org.apache.ws.commons.axiom", "axiom-impl"))
ThisBuild / libraryDependencies ++= Seq(
  // for the unit tests:
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test,

  "guru.nidi" % "graphviz-java" % "0.18.1",

  // logging related
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2")
ThisBuild / testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a"))
ThisBuild / assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith "logback.xml"
  => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// subprojects:
lazy val evee_elimination_proofs_core_owlapi4 = Project(
  base = file(createString("core", 4)),
  id = createString("core", 4)
)
  .settings(
    name := createString("core", 4),
    libraryDependencies ++= Seq(
//      my own stuff:
      "de.tu-dresden.inf.lat.evee" % "evee-data-owlapi4" % "0.1",
//      reasoner:
      "net.sourceforge.owlapi" % "org.semanticweb.hermit" % "1.3.8.413",
    )
  )

lazy val evee_elimination_proofs_core_owlapi5 = Project(
  base = file(createString("core", 5)),
  id = createString("core", 5)
)
  .settings(
    name := createString("core", 5),
    scalaSource in Compile := file(file(createScalaSRCDirString("core", 4)).getAbsolutePath),
//    target := file(file(createTargetDirString("core", 5)).getAbsolutePath),  //alternative to explicitly adding scalaSource
    libraryDependencies ++= Seq(
//       my own stuff:
      "de.tu-dresden.inf.lat.evee" % "evee-data-owlapi5" % "0.1",

//      "net.sourceforge.owlapi" % "owlapi-distribution" % "3.5.2",
//      "com.hermit-reasoner" % "org.semanticweb.hermit" % "1.3.8.4",
      "net.sourceforge.owlapi" % "org.semanticweb.hermit" % "1.4.3.517",
      "net.sourceforge.owlapi" % "owlapi-distribution" % "5.1.17",
    )
  )


lazy val evee_elimination_proofs_fame_owlapi4 = Project(
  base = file(createString("fame", 4)),
  id = createString("fame", 4)
)
  .settings(
    name := createString("fame", 4),
    unmanagedJars in Compile += file(createString("fame", 4) + "/../../lib/FamePlus/FamePlus/0.0.1-SNAPSHOT/FamePlus-0.0.1-SNAPSHOT.jar"),
    libraryDependencies ++= Seq(
//        for FAME
      "org.apache.commons" % "commons-lang3" % "3.6",
      "com.google.guava" % "guava" % "21.0",
    ))
  .dependsOn(evee_elimination_proofs_core_owlapi4)

lazy val evee_elimination_proofs_lethe_owlapi4 = Project(
  base = file(createString("lethe", 4)),
  id = createString("lethe", 4)
)
  .settings(
    name := createString("lethe", 4),
//    target := file(file(createTargetDirString("lethe", 4)).getAbsolutePath), //alternative to explicitly adding scalaSource
    unmanagedJars in Compile += file(createString("lethe", 4) + "/../../lib/de/tu-dresden/inf/lat/lethe-core_2.12/0.8-SNAPSHOT/lethe-core_2.12-0.8-SNAPSHOT.jar"),
    unmanagedJars in Compile += file(createString("lethe", 4) + "/../../lib/de/tu-dresden/inf/lat/lethe-owlapi4_2.12/0.8-SNAPSHOT/lethe-owlapi4_2.12-0.8-SNAPSHOT.jar")
  )
  .dependsOn(evee_elimination_proofs_core_owlapi4)

lazy val evee_elimination_proofs_lethe_owlapi5 = Project(
  base = file(createString("lethe", 5)),
  id = createString("lethe", 5)
)
  .settings(
    name := createString("lethe", 5),
    scalaSource in Compile := file(file(createScalaSRCDirString("lethe", 4)).getAbsolutePath),
    unmanagedJars in Compile += file(createString("lethe", 5) + "/../../lib/de/tu-dresden/inf/lat/lethe-core_2.12/0.8-SNAPSHOT/lethe-core_2.12-0.8-SNAPSHOT.jar"),
    unmanagedJars in Compile += file(createString("lethe", 5) + "/../../lib/de/tu-dresden/inf/lat/lethe-owlapi5_2.12/0.8-SNAPSHOT/lethe-owlapi5_2.12-0.8-SNAPSHOT.jar"),
    libraryDependencies ++= Seq(
      "net.sourceforge.owlapi" % "owlapi-distribution" % "5.1.17",
    ))
  .dependsOn(evee_elimination_proofs_core_owlapi5)

// easy string-generation to change names of directory/project/name
val baseDirectory : String = "evee-elimination-proofs-"
def createString(project : String, version : Int) : String = {
  baseDirectory + project + "-owlapi" + version
}

def createScalaSRCDirString(project : String, version : Int) : String = {
  createString(project, version) + "/src/main/scala"
}

def createTargetDirString(project : String, version : Int) : String = {
  createString(project, version) + "/target"
}
