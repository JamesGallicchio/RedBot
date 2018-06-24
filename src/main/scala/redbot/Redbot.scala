package redbot

import redbot.feed.FeedBot
import redbot.utils.discord.{BaseBot, ConversationBot}

import scala.io.Source

object Redbot {
  def main(args: Array[String]): Unit = {

    val propRegex = "(.+)=(.+)".r
    val tokens = Source.fromResource("tokens.txt").getLines()
      .flatMap(propRegex.findFirstMatchIn(_)).map(m => m.group(1) -> m.group(2)).toMap

    Seq(
      ("feedbot", (t: String) => new FeedBot(t))
    ).flatMap{ case (name, const) => tokens.get(name).map(const)}
  }
}

class Redbot extends ConversationBot {

}