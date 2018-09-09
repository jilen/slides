scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "1.0.0",
  "co.fs2" %% "fs2-core" % "1.0.0-M5",
  "com.github.shyiko" % "mysql-binlog-connector-java" % "0.16.1"
)
