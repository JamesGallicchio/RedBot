package redbot.bots

import redbot.cmd.{Command, UserMention}

abstract class CommandBot extends DiscordBot {
  def handler: Command => ()

  val prefix = UserMention(client.getSelfId)
  client.addMessageListener(msg => Command(prefix, msg, client).foreach(handler))
}
