package redbot.bots
import redbot.bots.CuteBot.SafetyLevel
import redbot.bots.CuteBot.SafetyLevel.{High, Medium, Off}
import redbot.cmd.Command
import redbot.discord.{Channel, Client, Permission}
import redbot.utils.DataStore

import scala.collection.mutable
import regex.Grinch

case class CuteBot(client: Client) extends CommandBot {
  private val dataIdent = "cutesafety"

  private val safeties: mutable.Map[Channel.Id, SafetyLevel] =
    DataStore.get(dataIdent).getOrElse(mutable.Map.empty)
  private def saveData(): Unit = DataStore.store(dataIdent, safeties)
  private def getSafety(ch: Channel.Id): SafetyLevel = safeties.getOrElse(ch, {
    safeties.update(ch, CuteBot.SafetyLevel.High)
    saveData()
    CuteBot.SafetyLevel.High
  })

  override val commands: Seq[Command] = Vector(
    Command("cute <search term(s)> [gif]", "Sends a random cute image; appending gif will limit the search to gifs")(cmd => {
      case gr"cute $terms(.+)" => cmd.reply(CuteBot.search(terms, getSafety(cmd.msg.channel)))
    }),
    Command("safety", "Displays this channel's safety level")(cmd => {
      case "safety" => val ch = cmd.channel
        cmd.reply(s"Safety level for ${Channel.mention(ch)}: ${getSafety(ch).desc}")
    }),
    Command("setsafety <off, medium, high [default]>", "Sets this channel's safety level (\"off\" won't filter NSFW content)")(cmd => {
      case gr"setsafety $level(off|medium|high)" =>
        cmd.hasPerms(Permission.ManageChannels) collect {
          case true => val ch = cmd.channel
            safeties.update(ch, level match {
              case "off" => Off
              case "medium" => Medium
              case "high" => High
            })
            saveData()
            cmd.reply(s"Safety level for ${Channel.mention(ch)}: ${getSafety(ch).desc}")
        }
    })
  )
}

object CuteBot {
  sealed trait SafetyLevel { def desc: String }
  object SafetyLevel {
    case object Off extends SafetyLevel { val desc: String = "OFF - all content allowed"}
    case object Medium extends SafetyLevel { val desc: String = "MEDIUM - some questionable content"}
    case object High extends SafetyLevel { val desc: String = "HIGH - no questionable content (like SafeSearch)"}
  }

  def search(terms: String, safetyLevel: SafetyLevel): String = ???
}