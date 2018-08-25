package redbot.bots
import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, TimeUnit}

import redbot.bots.CuteBot.SafetyLevel
import redbot.bots.CuteBot.SafetyLevel.{High, Medium, Off}
import redbot.cmd.Command
import redbot.discord.{Channel, Client, Permission}
import redbot.utils.{DataStore, Logger, TimerUtils}
import regex.Grinch

import scala.collection.mutable
import scala.concurrent.{Future, blocking}
import scala.io.Source
import scala.util.{Failure, Random, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

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
      case gr"cute $terms(.+)" =>
        CuteBot.search(terms, getSafety(cmd.msg.channel)).onComplete {
          case Success(linkOpt) => cmd.reply(linkOpt match {
            case Left(link) => s"CUTE ${terms.toUpperCase}: $link"
            case Right(error) => error
          })
          case Failure(e) => cmd.reply(e.getMessage)
        }
    }),

    Command("safety", "Displays this channel's safety level")(cmd => {
      case "safety" => val ch = cmd.channelId
        cmd.reply(s"Safety level for ${Channel.mention(ch)}: ${getSafety(ch).desc}")
    }),

    Command("setsafety <off, medium, high [default]>", "Sets this channel's safety level (\"off\" won't filter NSFW content)")(cmd => {
      case gr"setsafety $level(off|medium|high)" =>
        cmd.hasPerms(Permission.ManageChannels) collect {
          case true => val ch = cmd.channelId
            safeties.update(ch, level match {
              case "off" => Off
              case "medium" => Medium
              case "high" => High
            })
            saveData()
            cmd.reply(s"Safety level for ${Channel.mention(ch)}: ${getSafety(ch).desc}")
          case false =>
            cmd.reply("Only someone with the MANAGE_CHANNELS permission can edit the channel safety!")
        }
    })
  )
}

object CuteBot {
  sealed trait SafetyLevel {
    def desc: String
    def name: String
  }
  object SafetyLevel {
    case object Off extends SafetyLevel {
      val desc: String = "OFF - all content allowed"
      val name: String = "off"
    }
    case object Medium extends SafetyLevel {
      val desc: String = "MEDIUM - some questionable content"
      val name: String = "medium"
    }
    case object High extends SafetyLevel {
      val desc: String = "HIGH - no questionable content (like SafeSearch)"
      val name: String = "high"
    }
  }



  val engine: String = Source.fromResource("cuteengine.text").mkString

  val keys: IndexedSeq[String] = Source.fromResource("cutekeys.txt").getLines().toIndexedSeq
  private val len = keys.length
  private val idx = new AtomicInteger(0)
  private def nextKey() = keys(idx.getAndUpdate(i => (i+1)%len))



  // Common to every search
  private val baseSearchUrl = s"https://www.googleapis.com/customsearch/v1?cx=$engine&searchType=image&filter=1&num=1&fields=items%2Flink"

  // Add terms that are unique to each search, except for KEY- needs to be appended at end!
  private def searchUrl(encodedTerms: String, isGif: Boolean, safety: String, start: Int): String =
    baseSearchUrl + s"&q=cute+$encodedTerms${if (isGif) "&type=gif" else ""}&safe=$safety&start=$start&key="


  def search(terms: String, safetyLevel: SafetyLevel): Future[Either[String, String]] = {
    val isGif = terms.toLowerCase.endsWith("gif")

    val encoded = Try(URLEncoder.encode(terms.replaceAll(".","%2E"), "UTF-8")).get

    val startingPoint = Random.nextInt(100)

    val noKeyUrl = searchUrl(encoded, isGif, safetyLevel.name, startingPoint)


    import scala.concurrent.duration._

    TimerUtils.tryWithBackoff(10.millis,1.second) {
      Future {
        val url = noKeyUrl + nextKey()
        Source.fromURL(url).mkString match {
          case gr""""link":\s*"$link(.+)"""" => Left(link: String)
          case str => Right("This search turned up no results!")
        }
      }.recoverWith {
        // If got a 40x response -- probably means key has been used too much
        case e if e.getMessage.contains("40") =>
          Future.failed(new RuntimeException()) // Failure will cause retry

        // If got a 50x response
        case e if e.getMessage.contains("50") =>
          Future.successful(Right("Google's servers are probably down. Try again in a minute."))

        // Otherwise log the error
        case e =>
          Logger.log(e)("URL" -> noKeyUrl)
          Future.successful(Right("Unhandled exception occurred. Check RedBot support server for more information."))
      }
    }.recover { case e => Right("Probably reached daily limit :(") }
  }
}