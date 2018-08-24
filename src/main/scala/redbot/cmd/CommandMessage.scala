package redbot.cmd

import redbot.discord._

import scala.concurrent.Future

case class CommandMessage private(client: Client, msg: Message, user: User, content: String) {
  lazy val channel: Channel.Id = msg.channel

  def reply(content: String): Unit =
    client.sendMessage(msg.channel, User.mention(user) + " " + content)

  def hasPerms(ps: Permission*): Future[Boolean] =
    client.hasPermission(user.id, channel, ps:_*)

  lazy val args: List[String] = content.toLowerCase.split("\\s+").toList
}

object CommandMessage {
  def apply(client: Client, msg: Message, prefix: String): Option[CommandMessage] =
    for {
      a <- msg.author              // Ensure has author
      cont <- msg.content          // Ensure has content
      pidx = cont.indexOf(prefix)
      if pidx >= 0                 // Ensure uses prefix
      u <- client.getUser(a)
      if !u.isBot                  // Ensure isn't a bot
    } yield new CommandMessage(client, msg, u, cont.substring(pidx).trim)

  def unapply(arg: CommandMessage): Option[(Client, Message, User, Channel.Id, String)] =
    Some((arg.client, arg.msg, arg.user, arg.msg.channel, arg.content))
}