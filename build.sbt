import xerial.sbt.Sonatype.sonatypeSettings

lazy val root = project.in(file("."))
  .settings(
    Seq(
      name := """sbt-play-npm""",
      organization := "eu.unicredit",
      version := "0.1",
      crossSbtVersions := Seq("0.13.17", "1.1.0"),
      sbtPlugin := true,
      addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.9")
    ) ++ sonatypePublish
  )

lazy val sonatypePublish = sonatypeSettings ++ Seq(
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  publishTo := Some(sonatypeDefaultResolver.value),
  credentials += Credentials(Path.userHome / ".ivy2" / "sonatype.credentials"),
  pomExtra := {
    <url>https://github.com/unicredit/sbt-play-npm</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:github.com/unicredit/sbt-play-npm</connection>
        <developerConnection>scm:git:git@github.com:unicredit/sbt-play-npm</developerConnection>
        <url>github.com/unicredit/sbt-play-npm</url>
      </scm>
      <developers>
        <developer>
          <id>fralken</id>
          <name>Francesco Montecuccoli Degli Erri</name>
          <url>https://github.com/fralken/</url>
        </developer>
      </developers>
  }
)