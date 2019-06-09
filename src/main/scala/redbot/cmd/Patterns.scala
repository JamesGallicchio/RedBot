package redbot.cmd

import scala.util.Try
import scala.util.matching.Regex

object Patterns {

  object ChannelMention {

    import redbot.discord.Channel.Id

    val regex: Regex = "<#(\\d+)>".r

    def apply(arg: Id): String = s"<#$arg>"

    def unapply(arg: String): Option[Id] = for {
      m <- regex.findFirstMatchIn(arg)
    } yield java.lang.Long.parseUnsignedLong(m.group(1)).asInstanceOf[Id]
  }

  object UserMention {

    import redbot.discord.User.Id

    val regex: Regex = "<@!?(\\d+)>".r

    def apply(arg: Id): String = s"<@$arg>"

    def unapply(arg: String): Option[Id] = for {
      m <- regex.findFirstMatchIn(arg)
    } yield java.lang.Long.parseUnsignedLong(m.group(1)).asInstanceOf[Id]
  }

  object RoleMention {

    import redbot.discord.Role.Id

    val regex: Regex = "<@&(\\d+)>".r

    def apply(arg: Id): String = s"<@&$arg>"

    def unapply(arg: String): Option[Id] = for {
      m <- regex.findFirstMatchIn(arg)
    } yield java.lang.Long.parseUnsignedLong(m.group(1)).asInstanceOf[Id]
  }

  object Integer {
    def unapply(arg: String): Option[Int] = Try(arg.toInt).toOption
  }

}