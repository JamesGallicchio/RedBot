package redbot.cmd

import redbot.discord.Snowflake.Snowflake

import scala.util.matching.Regex

trait Mention {
  def unapply(arg: String): Option[Snowflake]
}

object ChannelMention extends Mention {
  import redbot.discord.Channel.Id

  val regex: Regex = "<#(\\d+)>".r
  def apply(arg: Id): String = s"<#$arg>"
  def unapply(arg: String): Option[Id] = for {
    m <- regex.findFirstMatchIn(arg)
  } yield java.lang.Long.parseUnsignedLong(m.group(1)).asInstanceOf[Id]
}

object UserMention extends Mention {
  import redbot.discord.User.Id

  val regex: Regex = "<@!?(\\d+)>".r
  def apply(arg: Id): String = s"<@$arg>"
  def unapply(arg: String): Option[Id] = for {
    m <- regex.findFirstMatchIn(arg)
  } yield java.lang.Long.parseUnsignedLong(m.group(1)).asInstanceOf[Id]
}

object RoleMention extends Mention {
  import redbot.discord.Role.Id

  val regex: Regex = "<@&(\\d+)>".r
  def apply(arg: Id): String = s"<@&$arg>"
  def unapply(arg: String): Option[Id] = for {
    m <- regex.findFirstMatchIn(arg)
  } yield java.lang.Long.parseUnsignedLong(m.group(1)).asInstanceOf[Id]
}