package redbot

import better.files._
import redbot.bots.{CuteBot, FeedBot, RedBot, CrushBot}
import redbot.discord.impl.d4j.D4JClient
import redbot.utils.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, blocking}

object Main {
  def main(args: Array[String]): Unit = {

    val tokenRegex = "(.+?)\\s+(.+)".r
    val tokens = (File.currentWorkingDirectory / "configs" / "tokens.txt").lines
      .flatMap(tokenRegex.findFirstMatchIn).map(m => m.group(1) -> m.group(2)).toMap

    val botTokens = Seq(
      //{RedBot.apply _} -> "test",
      //{FeedBot.apply _} -> "feed",
      //{CuteBot.apply _} -> "cute",
      {CrushBot.apply _} -> "crush"
    )

    val bots = botTokens.flatMap { case (constr, name) =>
      tokens.get(name).orElse(throw new RuntimeException(s"Missing token for $name."))
        .map { t => constr(new D4JClient(t)) }
    }

    bots.foreach { bot =>
      Future {
        Logger.info("Logging in bot " + bot.getClass.getSimpleName)
        blocking { bot.client.login() }
      }
    }

    // Wait for Ctrl+D (EOF) in command line
    Scanner.stdin.foreach {
      case x if x.exists(_ == EOF) =>
        Logger.info("Exiting JVM now")

        System.exit(0)
    }
  }
}
