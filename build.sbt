name := "RedBot"

version := "2.0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.9.0",
  "com.discord4j.discord4j" % "discord4j-core" % "c3ab5f8",
  "io.projectreactor" % "reactor-scala-extensions_2.12" % "0.3.5",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "io.github.soc" %% "regextractor" % "0.2",
  "com.github.pathikrit" %% "better-files" % "3.6.0",
  "com.typesafe.play" %% "play-json" % "2.6.10",
  "com.github.rometools" % "rome" % "1.7.1",
  "org.jsoup" % "jsoup" % "1.11.3"
)

resolvers ++= Seq(
  "jitpack.io" at "https://jitpack.io",
  "OSS Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "repository.spring.milestone" at "http://repo.spring.io/milestone"
)