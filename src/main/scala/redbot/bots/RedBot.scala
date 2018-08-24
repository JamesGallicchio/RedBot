package redbot.bots

import redbot.cmd._
import redbot.discord.Client

case class RedBot(client: Client) extends CommandBot {
  override def handler: PartialFunction[CommandMessage, Unit] = {case cmd @ Command(args, _, _) =>
    import redbot.cmd.Patterns._
    cmd.reply(args match {
      case "ping" :: _ => "pong"
      case "showid" :: rest => rest match {
        case ChannelMention(id) :: _ => id.toString
        case UserMention(id) :: _ => id.toString
        case RoleMention(id) :: _ => id.toString
      }
      case _ => "Unrecognized command!"
    })
  }
}
