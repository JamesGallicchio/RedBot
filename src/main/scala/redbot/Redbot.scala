package redbot

import redbot.bots.{DiscordBot, RedBot}
import redbot.discord.impl.d4j.Client

import scala.io.Source

object Redbot {
  def main(args: Array[String]): Unit = {

    val propRegex = "(.+)=(.+)".r
    val tokens = Source.fromResource("tokens.txt").getLines()
      .flatMap(propRegex.findFirstMatchIn(_)).map(m => m.group(1) -> m.group(2)).toMap

    val bots: Seq[DiscordBot] = Map(
      "redbot"  -> (RedBot.apply(_))
      //"feedbot" -> (FeedBot.apply(_))
    ).flatMap{ case (name, constr) => tokens.get(name).map(new Client(_)).map(constr) }.toSeq
  }
}