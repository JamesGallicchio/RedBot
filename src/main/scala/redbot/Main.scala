package redbot

import redbot.bots.{CuteBot, RedBot}
import redbot.discord.impl.d4j.Client

import scala.collection.parallel.ParSeq
import scala.io.Source

object Main {
  def main(args: Array[String]): Unit = {

    val tokenRegex = "(.+?)\\s+(.+)".r
    val tokens = Source.fromResource("tokens.txt").getLines()
      .flatMap(tokenRegex.findFirstMatchIn).map(m => m.group(1) -> m.group(2)).toMap

    val bots = ParSeq(
      {RedBot.apply _} -> "test",
      {CuteBot.apply _} -> "cute"
    )

    bots.flatMap { case (constr, name) =>
      tokens.get(name).orElse(throw new RuntimeException(s"Missing token for $name."))
        .map(new Client(_)).map(constr)
    }.foreach {
      bot =>
        new Thread {
          override def run(): Unit = {
            println("Logging in bot " + bot.getClass.getSimpleName)
            bot.client.login()
          }
        }.run()
    }
  }
}