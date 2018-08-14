package redbot.bots

import redbot.cmd.{ChannelMention, Command, Mention, UserMention}
import redbot.discord.Client

case class RedBot(client: Client) extends CommandBot {
  override def handler: Command => Unit = {case cmd @ Command(args, _, _) => args match {
    case "ping" :: _ => cmd.reply("pong")
    case "showid" :: mention :: _ => mention match {
      case ChannelMention(id) | UserMention(id2) => cmd.reply(id.toString)
    }
    case _ => cmd.reply("Unrecognized command!")
  }}
}
