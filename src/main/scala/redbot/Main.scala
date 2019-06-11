package redbot

import better.files._
import redbot.bots.{CuteBot, FeedBot, RedBot}
import redbot.discord.impl.d4j.Client
import redbot.utils.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Main {
  def main(args: Array[String]): Unit = {

    val tokenRegex = "(.+?)\\s+(.+)".r
    val tokens = (File.currentWorkingDirectory / "tokens.txt").lines
      .flatMap(tokenRegex.findFirstMatchIn).map(m => m.group(1) -> m.group(2)).toMap

    val botTokens = Seq(
      {RedBot.apply _} -> "test",
      {FeedBot.apply _} -> "feed",
      {CuteBot.apply _} -> "cute"
    )

    val bots = botTokens.flatMap { case (constr, name) =>
      tokens.get(name).orElse(throw new RuntimeException(s"Missing token for $name."))
        .map { t => constr(new Client(t)) }
    }

    bots.foreach { bot =>
      Future {
        Logger.debug("Logging in bot " + bot.getClass.getSimpleName)
        bot.client.login()
      }
    }

    // Wait for Ctrl+D (EOF) in command line
    while (System.in.read() != -1) {}
    Logger.info("Exiting JVM now")

    System.exit(0)
  }
}