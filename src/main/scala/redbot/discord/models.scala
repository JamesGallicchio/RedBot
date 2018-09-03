package redbot.discord

import java.time.Instant

import redbot.discord.Snowflake.Snowflake
import redbot.utils.OptParams.?

import scala.collection.TraversableOnce
import scala.concurrent.Future

object Snowflake {
  sealed trait SnowflakeTag
  type Snowflake = Long with SnowflakeTag

  implicit class Long2Snowflake(val s: Long) extends AnyVal {
    def asId[T <: redbot.discord.Snowflake.Snowflake]: T = s.asInstanceOf[T]
  }
}

abstract class Client(val token: String) {
  def login(): Unit

  def getSelfId: User.Id

  def getUser(id: User.Id): Future[User]

  def setPresence(presence: String): Unit

  def sendMessage(channel: Channel.Id, content: String): Unit
  def sendEmbed(channel: Channel.Id, embed: Embed): Unit

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

  def mention(c: Channel.Id): String = s"<#$c>"
}
object Role {
  sealed trait RoleTag
  type Id = Snowflake with RoleTag

  def mention(r: Role.Id): String = s"<&$r>"
}
object Guild {
  sealed trait GuildTag
  type Id = Snowflake with GuildTag
}

sealed trait Permission
object Permission {
  case object ManageChannels extends Permission
}

case class Embed(title: ?[String] = ?,
                 description: ?[String] = ?,
                 url: ?[String] = ?,
                 timestamp: ?[Instant] = ?,
                 color: ?[Integer] = ?,
                 footer: ?[EmbedFooter] = ?,
                 imageUrl: ?[String] = ?,
                 thumbnailUrl: ?[String] = ?,
                 author: ?[EmbedAuthor] = ?,
                 fields: Traversable[EmbedField] = Traversable.empty
                ) {
  def copy(title: ?[String] = title,
           description: ?[String] = description,
           url: ?[String] = url,
           timestamp: ?[Instant] = timestamp,
           color: ?[Integer] = color,
           footer: ?[EmbedFooter] = footer,
           imageUrl: ?[String] = imageUrl,
           thumbnailUrl: ?[String] = thumbnailUrl,
           author: ?[EmbedAuthor] = author,
           fields: Traversable[EmbedField] = fields
          ): Embed = Embed(title, description, url, timestamp, color, footer, imageUrl, thumbnailUrl, author, fields)
}
case class EmbedFooter(text: String, iconUrl: ?[String] = ?)
case class EmbedAuthor(name: String, url: ?[String] = ?, iconUrl: ?[String] = ?)
case class EmbedField(name: String, value: String, inline: Boolean)