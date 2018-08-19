package redbot.cmd

import redbot.discord.{Client, Message, User}

case class Command (args: List[String], msg: Message, client: Client) {

  def user: User.Id =
    msg.author.getOrElse(throw new IllegalStateException("message author empty"))
  def reply(content: String): Unit =
    client.sendMessage(msg.channel, User.mention(user) + " " + content)
}