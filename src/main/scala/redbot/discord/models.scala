package redbot.discord

import redbot.discord.Snowflake.Snowflake

import scala.concurrent.Future

object Snowflake {
  private trait SnowflakeTag
  type Snowflake = Long with SnowflakeTag
}

abstract class Client(val token: String) {
  def getSelfId: User.Id

  def getUser(id: User.Id): Future[User]

  def setPresence(presence: String): Unit

  def sendMessage(channel: Channel.Id, content: String): Unit
  def addMessageListener(handler: Message => Any): Unit
}

trait Message {
  def id: Message.Id
  def content: Option[String]
  def author: Option[User.Id]
  def channel: Channel.Id
}
object Message {
  private trait MessageTag
  type Id = Snowflake with MessageTag
}

trait User {
  def id: User.Id
  def username: String
  def isBot: Boolean
}
object User {
  private trait UserTag
  type Id = Snowflake with UserTag
}

object Channel {
  private trait ChannelTag
  type Id = Snowflake with ChannelTag
}
object Role {
  private trait RoleTag
  type Id = Snowflake with RoleTag
}
object Guild {
  private trait GuildTag
  type Id = Snowflake with GuildTag
}