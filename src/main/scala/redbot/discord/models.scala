package redbot.discord

import redbot.discord.Snowflake.Snowflake

import scala.concurrent.Future

object Snowflake {
  sealed trait SnowflakeTag
  type Snowflake = Long with SnowflakeTag
}

abstract class Client(val token: String) {
  def login(): Unit

  def getSelfId: User.Id

  def getUser(id: User.Id): Future[User]

  def setPresence(presence: String): Unit

  def sendMessage(channel: Channel.Id, content: String): Unit
  def addMessageListener(handler: Message => Unit): Unit

  def hasPermission(u: User.Id, c: Channel.Id, ps: Permission*): Future[Boolean]
}

trait Message extends Any {
  def id: Message.Id
  def content: Option[String]
  def author: Option[User.Id]
  def channel: Channel.Id
}
object Message {
  sealed trait MessageTag
  type Id = Snowflake with MessageTag

  def unapply(arg: Message): Option[(Message.Id,Channel.Id,Option[String],Option[User.Id])] =
    Some(arg.id, arg.channel, arg.content, arg.author)
}

trait User extends Any {
  def id: User.Id
  def username: String
  def isBot: Boolean
}
object User {
  sealed trait UserTag
  type Id = Snowflake with UserTag

  def mention(u: User): String = mention(u.id)
  def mention(u: User.Id): String = s"<@$u>"
}

object Channel {
  sealed trait ChannelTag
  type Id = Snowflake with ChannelTag

  def mention(c: Channel.Id): String = s"<@$c>"
}
object Role {
  sealed trait RoleTag
  type Id = Snowflake with RoleTag

  def mention(r: Role.Id): String = s"<@$r>"
}
object Guild {
  sealed trait GuildTag
  type Id = Snowflake with GuildTag
}

sealed trait Permission
object Permission {
  case object ManageChannels extends Permission
}