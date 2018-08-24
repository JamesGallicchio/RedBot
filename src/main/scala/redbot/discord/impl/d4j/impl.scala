package redbot.discord.impl.d4j

import discord4j.core.`object`.presence.{Activity, Presence}
import discord4j.core.`object`.util.Snowflake
import discord4j.core.`object`.{entity => d4j}
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.MessageCreateSpec
import discord4j.core.{DiscordClient, DiscordClientBuilder}
import redbot.discord.impl.d4j.JavaConversions._
import redbot.discord.impl.d4j.SnowflakeConversions._
import redbot.{discord => red}

import scala.concurrent.Future

final class Client(private val tok: String) extends red.Client(tok) {

  private val client: DiscordClient = new DiscordClientBuilder(token)
    .setInitialPresence(Presence.online(Activity.playing(s"Mention me to get started!")))
    .build()

  override def login(): Unit =
    client.login().block()

  override lazy val getSelfId: red.User.Id = client.getSelfId
    .orElseThrow(() => new IllegalStateException("Tried to get self id before client was logged in!"))
    .as[red.User.Id]

  override def getUser(id: red.User.Id): Future[red.User] =
    client.getUserById(Snowflake.of(id)).toScala.map(new User(_)).toFuture

  override def sendMessage(channel: red.Channel.Id, content: String): Unit =
    client.getTextChannelById(Snowflake.of(channel)).flatMap(
      _.createMessage(new MessageCreateSpec().setContent(content))
    ).subscribe()

  override def addMessageListener(handler: red.Message => Unit): Unit =
    client.getEventDispatcher.on(classOf[MessageCreateEvent])
    .subscribe{e => handler(new Message(e.getMessage))}

  override def setPresence(presence: String): Unit =
    client.updatePresence(Presence.online(Activity.playing(presence))).subscribe()

  override def hasPermission(u: red.User.Id, c: red.Channel.Id, ps: red.Permission*): Future[Boolean] =
    ???
}

final class Message(private val msg: d4j.Message) extends AnyVal with red.Message {
  override def id: red.Message.Id = msg.getId.as[red.Message.Id]

  override def content: Option[String] =
    msg.getContent.toScala

  override def author: Option[red.User.Id] =
    msg.getAuthorId.toScala.map(_.as[red.User.Id])

  override def channel: red.Channel.Id =
    msg.getChannelId.as[red.Channel.Id]
}

final class User(private val u: d4j.User) extends AnyVal with red.User {
  override def id: red.User.Id = u.getId.as[red.User.Id]
  override def isBot: Boolean = u.isBot
  override def username: String = u.getUsername
}