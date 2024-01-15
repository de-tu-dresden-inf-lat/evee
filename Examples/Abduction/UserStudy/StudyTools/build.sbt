ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "study-tools"
  )

// https://mvnrepository.com/artifact/net.sourceforge.owlapi/owlapi-distribution
libraryDependencies += "net.sourceforge.owlapi" % "owlapi-distribution" % "5.5.0"


ThisBuild / assembly / assemblyMergeStrategy := {
  //  case PathList("net.sourceforge.owlapi", "owlapi-distribution", xs @
  //      _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  //  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  //  case "application.conf"                            => MergeStrategy.concat
  //  case "unwanted.txt"                                =>
  //  MergeStrategy.discard
  case "META-INF/axiom.xml" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}