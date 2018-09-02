package redbot.cmd

import redbot.discord._

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

case class CommandMessage private(client: Client, msg: Message, user: User, content: String) {
  lazy val channelId: Channel.Id = msg.channel

  def reply(content: String): Unit =
    client.sendMessage(msg.channel, User.mention(user) + " " + content)

  def hasPerms(ps: Permission*): Future[Boolean] =
    client.hasPermission(user.id, channelId, ps:_*)

  def checkPerms(ps: Permission*)(onSuccess: => ()) =
    hasPerms(ps:_*) collect {
      case true => onSuccess
      case false => val permList = ps.map(_.getClass.getSimpleName).mkString(",")
        reply(s"You don't have permission to do that. Requires: $permList")
    }

  lazy val args: List[String] = content.toLowerCase.split("\\s+").toList
}

object CommandMessage {
  def apply(client: Client, msg: Message, prefix: String): Future[Option[CommandMessage]] =
    (for {
      a <- msg.author // Ensure has author
      cont <- msg.content // Ensure has content
      pidx = cont.indexOf(prefix)
      if pidx >= 0 // Ensure uses prefix
    } yield client.getUser(a).map {
      case u if !u.isBot => // Ensure isn't a bot
        Some(new CommandMessage(client, msg, u, cont.substring(pidx+prefix.length).trim))
      case _ => None
    }).getOrElse(Future.successful(None)) // Reduce outer monad

  def unapply(arg: CommandMessage): Option[(Client, Message, User, Channel.Id, String)] =
    Some((arg.client, arg.msg, arg.user, arg.msg.channel, arg.content))
}