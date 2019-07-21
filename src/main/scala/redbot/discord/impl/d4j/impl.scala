package redbot.discord.impl.d4j

import java.awt.Color

import discord4j.core.`object`.entity.{MessageChannel, TextChannel}
import discord4j.core.`object`.presence.{Activity, Presence}
import discord4j.core.`object`.util.{Permission, Snowflake}
import discord4j.core.`object`.{entity => d4j}
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.{DiscordClient, DiscordClientBuilder}
import redbot.discord.Channel.Id
import redbot.discord.Embed
import redbot.discord.impl.d4j.JavaConversions._
import redbot.discord.impl.d4j.SnowflakeConversions._
import redbot.{discord => red}

//import scala.jdk.CollectionConverters._
import scala.collection.JavaConverters._
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
    client.getUserById(Snowflake.of(id)).asScala.map(new User(_))
      .toFuture

  override def sendMessage(channel: red.Channel.Id, content: String): Unit =
    client.getChannelById(Snowflake.of(channel)).flatMap(
      _.asInstanceOf[MessageChannel].createMessage(content)
    ).subscribe()

  override def sendEmbed(channel: Id, embed: Embed): Unit =
    client.getChannelById(Snowflake.of(channel))
      .flatMap(_.asInstanceOf[TextChannel].createEmbed(spec => {
        embed.title.map(spec.setTitle)
        embed.description.map(spec.setDescription)
        embed.url.map(spec.setUrl)
        embed.timestamp.map(spec.setTimestamp)
        embed.color.map(c => spec.setColor(new Color(c)))
        embed.footer.map(f => spec.setFooter(f.text, f.iconUrl.orNull))
        embed.imageUrl.map(spec.setImage)
        embed.thumbnailUrl.map(spec.setThumbnail)
        embed.author.map(a => spec.setAuthor(a.name, a.url.orNull, a.iconUrl.orNull))
        embed.fields.map(f => spec.addField(f.name, f.value, f.inline))
      })).subscribe()

  override def addMessageListener(handler: red.Message => Unit): Unit =
    client.getEventDispatcher.on(classOf[MessageCreateEvent])
      .subscribe{e => handler(new Message(e.getMessage))}

  override def setPresence(presence: String): Unit =
    client.updatePresence(Presence.online(Activity.playing(presence)))
      .subscribe()

  override def hasPermission(u: red.User.Id, c: red.Channel.Id, ps: red.Permission*): Future[Boolean] = {
    val d4jPermList = ps.map {
      case red.Permission.ManageChannels => Permission.MANAGE_CHANNELS
    }.asJava
    client.getChannelById(Snowflake.of(c)).asScala.
      flatMap(_.asInstanceOf[TextChannel].getEffectivePermissions(Snowflake.of(u)).asScala).
      map(_.containsAll(d4jPermList)).toFuture
  }
}

final class Message(private val msg: d4j.Message) extends AnyVal with red.Message {
  override def id: red.Message.Id = msg.getId.as[red.Message.Id]

  override def content: Option[String] =
    msg.getContent.toScala

  override def author: Option[red.User.Id] =
    msg.getAuthor.toScala.map(_.getId.as[red.User.Id])

  override def channel: red.Channel.Id =
    msg.getChannelId.as[red.Channel.Id]
}

final class User(private val u: d4j.User) extends AnyVal with red.User {
  override def id: red.User.Id = u.getId.as[red.User.Id]
  override def isBot: Boolean = u.isBot
  override def username: String = u.getUsername
}