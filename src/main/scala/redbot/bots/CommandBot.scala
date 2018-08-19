package redbot.bots

import redbot.cmd.Command
import redbot.discord.User
import scala.concurrent.ExecutionContext.Implicits.global

abstract class CommandBot extends DiscordBot {
  def handler: PartialFunction[Command, Any]

  lazy val prefix: String = User.mention(client.getSelfId)

  client.addMessageListener { msg =>
    for {
      a <- msg.author // Ensure has author
      cont <- msg.content // Ensure has content
      if cont.length > prefix.length &&
        cont.startsWith(prefix) // Ensure starts with prefix
      u <- client.getUser(a)
      if !u.isBot
      args = cont.toLowerCase.split("\\w", prefix.length).toList
    } handler(Command(args, msg, client))
  }
}