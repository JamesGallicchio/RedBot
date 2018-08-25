package redbot.bots

import redbot.cmd._
import redbot.discord.Client

import regex.Grinch

case class RedBot(client: Client) extends CommandBot {
  val commands = Seq(
    Command("ping", "Sends back a pong")(msg => {
      case text if text.startsWith("ping") => msg.reply("Pong.")
    }),

    Command("showid <mention>", "Extracts the ID of the mention")(msg => {
      case gr"showid $rest(.+)" =>
        import redbot.cmd.Patterns._
        msg.reply(rest match {
          case ChannelMention(id) => id.toString
          case UserMention(id) => id.toString
          case RoleMention(id) => id.toString
          case _ => "Mention an object to extract its ID: `showid <mention>`"
        })
    })
  )
}
