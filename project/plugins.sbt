resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// https://mvnrepository.com/artifact/org.postgresql/postgresql
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.16"

// Flyway Plugin
// https://github.com/flyway/flyway-sbt
addSbtPlugin("io.github.davidmweber" % "flyway-sbt" % "6.5.0")

// Slick CodeGen
// https://github.com/tototoshi/sbt-slick-codegen
addSbtPlugin("com.github.tototoshi" % "sbt-slick-codegen" % "1.4.0")

// Play Plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.2")

// Giter8 Plugin
addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.11.0")

// Scala Formatter Plugin
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.2.1")
