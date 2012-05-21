name := "hello"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Repo Codahale for Logula" at "http://repo.codahale.com"

libraryDependencies += "org.apache.jena" % "jena-core" % "2.7.0-incubating"

libraryDependencies += "org.apache.jena" % "jena-arq" % "2.9.0-incubating"

libraryDependencies += "com.codahale" %% "logula" % "2.1.3"