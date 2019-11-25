package redbot.discord

import java.time.Instant

import redbot.discord.Snowflake.Snowflake
import redbot.utils.OptParams.?

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

  def getChannel(id: Channel.Id): Future[Channel]

  def getPM(id: User.Id): Future[Channel.Id]

  def getGuilds(): Stream[Guild.Id]

  def getMembers(id: Guild.Id): Stream[User.Id]

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
  def discrim: String
  def isBot: Boolean
}
object User {
  sealed trait UserTag
  type Id = Snowflake with UserTag

  def mention(u: User): String = mention(u.id)
  def mention(u: User.Id): String = s"<@$u>"
}

trait Channel extends Any {
  def id: Channel.Id
  def isPM: Boolean
}
object Channel {
  sealed trait ChannelTag
  type Id = Snowflake with ChannelTag

  def mention(c: Channel): String = mention(c.id)
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

  val DM_PERMISSION_SET: Set[Permission] = Set(
    ManageChannels
  )
}

case class Embed(title:        ?[String]       = None,
                 description:  ?[String]       = None,
                 url:          ?[String]       = None,
                 timestamp:    ?[Instant]      = None,
                 color:        ?[Integer]      = None,
                 footer:       ?[EmbedFooter]  = None,
                 imageUrl:     ?[String]       = None,
                 thumbnailUrl: ?[String]       = None,
                 author:       ?[EmbedAuthor]  = None,
                 fields: Iterable[EmbedField] = Iterable.empty
                ) {
  def copy(title:        ?[String]      = title,
           description:  ?[String]      = description,
           url:          ?[String]      = url,
           timestamp:    ?[Instant]     = timestamp,
           color:        ?[Integer]     = color,
           footer:       ?[EmbedFooter] = footer,
           imageUrl:     ?[String]      = imageUrl,
           thumbnailUrl: ?[String]      = thumbnailUrl,
           author:       ?[EmbedAuthor] = author,
           fields: Iterable[EmbedField] = fields
          ): Embed = Embed(title, description, url, timestamp, color, footer, imageUrl, thumbnailUrl, author, fields)
}
case class EmbedFooter(text: String, iconUrl: ?[String] = None)
case class EmbedAuthor(name: String, url: ?[String] = None, iconUrl: ?[String] = None)
case class EmbedField(name: String, value: String, inline: Boolean)
