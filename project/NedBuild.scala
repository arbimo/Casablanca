import sbt._
import Keys._

object NedBuild extends Build {
  lazy val sbtIdea = Project("root", file("."), settings = mainSettings)

  lazy val mainSettings: Seq[Project.Setting[_]] = Defaults.defaultSettings ++ Seq(
    organization := "br.ufrj.ned",
    name := "Ufrj-Ned",
    version := "0.1",
    scalacOptions ++= Seq("-deprecation", "-unchecked")
  )
}