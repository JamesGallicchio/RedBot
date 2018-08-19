package redbot

import redbot.bots.RedBot
import redbot.discord.impl.d4j.Client

import scala.collection.parallel.ParSeq
import scala.io.Source

object Redbot {
  def main(args: Array[String]): Unit = {

    val tokenRegex = "(.+) +(.+)".r
    val tokens = Source.fromResource("tokens.txt").getLines()
      .flatMap(tokenRegex.findFirstMatchIn).map(m => m.group(1) -> m.group(2)).toMap

    println(tokens)

    ParSeq(
      "testbot"  -> {c: redbot.discord.Client => println("makin"); RedBot.apply(c)}
      //"feedbot" -> (FeedBot.apply(_))
    ).flatMap{ case (name, constr) =>
      tokens.get(name).orElse(throw new RuntimeException(s"Missing token for $name."))
        .map(new Client(_)).map(constr)
    }.foreach(_.client.login())
  }
}