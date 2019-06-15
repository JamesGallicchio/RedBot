name := "RedBot"

version := "2.0.1"

scalaVersion := "2.13.0"

run / fork := true
run / connectInput := true

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0",
  "com.discord4j.discord4j" % "discord4j-core" % "3.0.6",
  "io.projectreactor" % "reactor-scala-extensions_2.13.0-M3" % "0.4.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "io.github.jamesgallicchio" %% "regextractor" % "0.2",
  "com.github.pathikrit" %% "better-files" % "3.8.0",
  "com.typesafe.play" %% "play-json" % "2.7.4",
  "com.github.rometools" % "rome" % "1.12.0",
  "org.jsoup" % "jsoup" % "1.12.1",
  "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0"
)

resolvers ++= Seq(
  "jitpack.io" at "https://jitpack.io",
  "OSS Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "repository.spring.milestone" at "http://repo.spring.io/milestone"
)