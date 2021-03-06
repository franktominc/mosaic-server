import com.typesafe.sbt.packager.docker.Cmd

// Library versions all in one place, for convenience and sanity.
lazy val attoVersion          = "0.6.3"
lazy val kindProjectorVersion = "0.9.7"
lazy val fs2CoreVersion       = "1.0.0-M4"
lazy val jettyVersion         = "9.4.11.v20180605"
lazy val catsParVersion       = "0.2.0"

// sbt-header requires these settings even though we're using a custom license header
organizationName in ThisBuild := "Association of Universities for Research in Astronomy, Inc. (AURA)"
startYear        in ThisBuild := Some(2018)
licenses         in ThisBuild += ("BSD-3-Clause", new URL("https://opensource.org/licenses/BSD-3-Clause"))

// run dependencyUpdates whenever we [re]load. Spooky eh?
// onLoad in Global := { s => "dependencyUpdates" :: s }

lazy val scalacSettings = Seq(
  scalaVersion := "2.12.6",
  scalacOptions ++= Seq(
    "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
    "-encoding", "utf-8",                // Specify character encoding used by source files.
    "-explaintypes",                     // Explain type errors in more detail.
    "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
    "-language:higherKinds",             // Allow higher-kinded types
    "-language:implicitConversions",     // Allow definition of implicit functions called views
    "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
    "-Xfuture",                          // Turn on future language features.
    "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
    "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
    "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
    "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
    "-Xlint:option-implicit",            // Option.apply used implicit view.
    "-Xlint:package-object-classes",     // Class or object defined in package object.
    "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match",              // Pattern match may not be typesafe.
    "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    // "-Yno-imports",                      // No predef or default imports
    "-Ypartial-unification",             // Enable partial unification in type constructor inference
    "-Ywarn-dead-code",                  // Warn when dead code is identified.
    "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
    "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen",              // Warn when numerics are widened.
    "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals",              // Warn if a local definition is unused.
    "-Ywarn-unused:params",              // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates",            // Warn if a private member is unused.
    "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.
  ),
  scalacOptions in (Test, compile) --= Seq(
    "-Ywarn-unused:privates",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:imports",
    "-Yno-imports"
  ),
  scalacOptions in (Compile, console) --= Seq("-Xfatal-warnings", "-Ywarn-unused:imports", "-Yno-imports"),
  scalacOptions in (Compile, doc)     --= Seq("-Xfatal-warnings", "-Ywarn-unused:imports", "-Yno-imports")
)

lazy val commonSettings = scalacSettings ++ Seq(
  organization := "edu.gemini",
  licenses ++= Seq(("MIT", url("http://opensource.org/licenses/MIT"))),

  // These sbt-header settings can't be set in ThisBuild for some reason
  headerMappings := headerMappings.value + (HeaderFileType.scala -> HeaderCommentStyle.cppStyleLineComment),
  headerLicense  := Some(HeaderLicense.Custom(
    """|Copyright (c) 2016-2018 Association of Universities for Research in Astronomy, Inc. (AURA)
        |For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause
        |""".stripMargin
  )),

  addCompilerPlugin("org.spire-math" %% "kind-projector" % kindProjectorVersion),

)

lazy val mosaic_server = project.in(file("."))
  .settings(commonSettings)
  .dependsOn(core)
  .aggregate(core)

lazy val core = project
  .in(file("modules/core"))
  .settings(commonSettings)
  .settings(
    name        := "mosaic-server-core",
    description := "Mosaic image server based on Montage.",
    libraryDependencies ++= Seq(
      "org.tpolecat"      %% "atto-core"    % attoVersion,
      "co.fs2"            %% "fs2-core"     % fs2CoreVersion,
      "io.chrisdavenport" %% "cats-par"     % catsParVersion,
      "org.eclipse.jetty" %  "jetty-server" % jettyVersion
    ),
    scalacOptions += "-Yno-predef"
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    Docker / packageName := "web",
    dockerRepository := Some("registry.heroku.com"),
    dockerUsername   := Some("gemini-2mass-mosaic"),
    name in Docker   := "web",
    dockerAlias      := DockerAlias(
      dockerRepository.value,
      dockerUsername.value,
      (name in Docker).value,
      None
    ),
    dockerCommands   := """

      FROM ubuntu:bionic

      # Install the things we need
      RUN apt-get update
      RUN apt-get install --yes build-essential git libfontconfig openjdk-8-jre

      # Build Montage from Rob's fork and add it to the path
      RUN git clone -b mArchiveList-segfault https://github.com/tpolecat/Montage.git
      WORKDIR /Montage
      RUN make
      WORKDIR /
      ENV PATH="${PATH}:/Montage/bin"

      # Set up the Scala app
      WORKDIR /opt/docker
      ADD --chown=daemon:daemon opt /opt
      USER daemon
      CMD /opt/docker/bin/mosaic-server-core

    """.lines.map(_.trim).map(Cmd(_)).toSeq
  )

