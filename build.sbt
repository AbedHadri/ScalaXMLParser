name := "XMLParser"

version := "0.1"

scalaVersion := "2.12.5"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"
libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "3.4.0"
libraryDependencies +=  "org.slf4j" % "slf4j-api" % "1.7.5"
libraryDependencies +=  "org.slf4j" % "slf4j-simple" % "1.7.5"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"