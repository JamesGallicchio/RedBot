package redbot.cmd

import redbot.discord.{Client, Message, User}

case class Command private (args: List[String], msg: Message, client: Client) {

  def user: User.Id =
    msg.author.getOrElse(throw new IllegalStateException("message author empty"))
  def reply(content: String): Unit =
    client.sendMessage(msg.channel, UserMention(user) + " " + content)
}
object Command {
  def apply(prefix: String, msg: Message, client: Client): Option[Command] = for {
    a <- msg.author                   // Ensure has author
    cont <- msg.content               // Ensure has content
    if cont.length > prefix.length &&
      cont.startsWith(prefix)         // Ensure starts with prefix
    u <- client.getUser(a)
    if !u.isBot                       // Ensure isn't a bot user
  } yield new Command(cont.split(" ", prefix.length).toList, msg, client)
}