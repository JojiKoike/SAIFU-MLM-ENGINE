import _root_.slick.codegen.SourceCodeGenerator
import _root_.slick.{model => m}

lazy val databaseUrl =
  sys.env.getOrElse("DATABASE_JDBC_URL", "jdbc:postgresql:saifu_mlm_engine_db")
lazy val databaseUser =
  sys.env.getOrElse("DATABASE_USER", "saifu_mlm_engine_db_user")
lazy val databasePassword =
  sys.env.getOrElse("DATABASE_PASSWORD", "password")

val FlywayVersion = "6.5.5"

ThisBuild / organization := "com.saifu_mlm"
ThisBuild / version := "0.0.1-SNAPSHOT"

ThisBuild / resolvers += Resolver.sonatypeRepo("releases")
ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")

ThisBuild / libraryDependencies ++= Seq(
  "javax.inject"      % "javax.inject" % "1",
  "joda-time"         % "joda-time"    % "2.10.6",
  "org.joda"          % "joda-convert" % "2.2.1",
  "com.google.inject" % "guice"        % "4.2.3"
)
ThisBuild / javacOptions ++= Seq("-source", "1.11", "-target", "1.11")

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-numeric-widen"
)

// Database Migration
lazy val flyway = (project in file("modules/flyway"))
  .enablePlugins(FlywayPlugin)
  .settings(
    libraryDependencies += "org.flywaydb" % "flyway-core" % FlywayVersion,
    flywayLocations := Seq("classpath:db/migration"),
    flywayUrl := databaseUrl,
    flywayUser := databaseUser,
    flywayPassword := databasePassword,
    flywayBaselineOnMigrate := true
  )

// API for DAO
lazy val apidao = project in file("modules/apidao")

// DAO Code Generation
lazy val slick = (project in file("modules/slick"))
  .enablePlugins(CodegenPlugin)
  .settings(
    libraryDependencies ++= Seq(
        "com.zaxxer"            % "HikariCP"          % "3.4.5",
        "com.typesafe.slick"   %% "slick"             % "3.3.2",
        "com.typesafe.slick"   %% "slick-hikaricp"    % "3.3.2",
        "com.github.tototoshi" %% "slick-joda-mapper" % "2.4.2"
      ),
    slickCodegenDatabaseUrl := databaseUrl,
    slickCodegenDatabaseUser := databaseUser,
    slickCodegenDatabasePassword := databasePassword,
    slickCodegenDriver := _root_.slick.jdbc.PostgresProfile,
    slickCodegenJdbcDriver := "org.postgresql.Driver",
    slickCodegenOutputPackage := "com.saifu_mlm.infrastructure.db.slick",
    slickCodegenExcludedTables := Seq("flyway_schema_history"),
    slickCodegenCodeGenerator := { model: m.Model =>
      new SourceCodeGenerator(model) {
        override def code =
          "import com.github.tototoshi.slick.PostgresJodaSupport._\n" +
          "import org.joda.time.DateTime\n" +
          super.code

        override def Table =
          new Table(_) {
            override def Column =
              new Column(_) {
                override def rawType: String =
                  model.tpe match {
                    case "java.sql.Timestamp" => "DateTime"
                    case _                    => super.rawType
                  }
              }
          }
      }
    },
    sourceGenerators in Compile += slickCodegen.taskValue
  )
  .dependsOn(apidao)
  .aggregate(apidao)

// Main
lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """saifu-mlm-engine""",
    libraryDependencies ++= Seq(
        guice,
        "net.logstash.logback"    % "logstash-logback-encoder" % "6.2",
        "org.postgresql"          % "postgresql"               % "42.2.16",
        "org.flywaydb"            % "flyway-core"              % FlywayVersion % Test,
        "org.scalatestplus.play" %% "scalatestplus-play"       % "5.0.0"       % Test
      ),
    fork in Test := true
  )
  .aggregate(slick)
  .dependsOn(slick)

// Code Formatter
ThisBuild / scalafmtConfig := file(".scalafmt.conf")

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.saifu-mlm.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.saifu-mlm.binders._"
