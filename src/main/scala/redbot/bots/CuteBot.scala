package redbot.bots
import java.io.IOException
import java.net.{URL, URLEncoder}
import java.util.concurrent.atomic.AtomicInteger

import better.files._
import play.api.libs.json._
import redbot.bots.CuteBot.SafetyLevel
import redbot.bots.CuteBot.SafetyLevel.{High, Medium, Off}
import redbot.cmd.Command
import redbot.discord.{Channel, Client, Permission}
import redbot.discord.Snowflake._
import redbot.utils.{DataStore, Logger, TimerUtils}
import regex.Grinch

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Random, Success, Try}

case class CuteBot(client: Client) extends CommandBot {

  override val commands: Seq[Command[_]] = Vector(
    Command("cute <search term(s)> [gif]", "Sends a random cute image; appending gif will limit the search to gifs")(cmd => {
      case msg if msg.startsWith("cute") => val terms = msg.drop(4).trim
        CuteBot.search(terms, getSafety(cmd.msg.channel)).onComplete {
          case Success(link) => cmd.reply(s"CUTE ${terms.toUpperCase}: $link")
          case Failure(e) => cmd.reply(e.getMessage)
        }
    }),

    Command("safety", "Displays this channel's safety level")(cmd => {
      case "safety" => val ch = cmd.channelId
        cmd.reply(s"Safety level for ${Channel.mention(ch)}: ${getSafety(ch).desc}")
    }),

    Command("setsafety <off, medium, [high]>", "Sets this channel's safety level (\"off\" won't filter NSFW content)")(cmd => {
      case gr"setsafety $level(off|medium|high)" =>
        cmd.checkPerms(Permission.ManageChannels) {
          val ch = cmd.channelId
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

  private val safetiesIdent = "cute_safeties"
  private type safetiesType = mutable.Map[Channel.Id, SafetyLevel]

  import DataStore.Implicits.{mutableMapReads, mutableMapWrites, uLongMapReads, uLongMapWrites}

  implicit val safetyLevelReads: Reads[SafetyLevel] = (json: JsValue) =>
    Reads.StringReads.reads(json).map(_.toLowerCase match {
      case "off" => Off
      case "medium" => Medium
      case "high" => High
      case _ => throw new IllegalArgumentException("Invalid SafetyLevel name!")
    })
  implicit val safetyLevelWrites: Writes[SafetyLevel] = (o: SafetyLevel) => JsString(o.name)

  private implicit val safetiesReads: Reads[safetiesType] =
    implicitly[Reads[Map[Long, SafetyLevel]]] // Read a Map[Long, SafetyLevel]
      .map(longMap => TrieMap( // Convert Map to TrieMap
        longMap.toSeq.map{
          case (l, sl) => (l.asId, sl) // Convert Long to Channel.Id
        }:_*))

  private val safeties: safetiesType = DataStore.get[safetiesType](safetiesIdent).getOrElse(mutable.Map.empty)
  private def saveData(): Unit = DataStore.store(safetiesIdent, safeties)
  private def getSafety(ch: Channel.Id): SafetyLevel = safeties.getOrElse(ch, {
    safeties.update(ch, CuteBot.SafetyLevel.High)
    saveData()
    CuteBot.SafetyLevel.High
  })


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

  case class CuteException(override val getMessage: String) extends RuntimeException


  val engine: String = Resource.getAsString("cuteengine.txt")

  val keys: IndexedSeq[String] = Resource.getAsStream("cutekeys.txt").lines.toIndexedSeq
  private val len = keys.length
  private val idx = new AtomicInteger(0)
  private def nextKey() = keys(idx.getAndUpdate(i => (i+1)%len))



  // Common to every search
  private val baseSearchUrl = s"https://www.googleapis.com/customsearch/v1?cx=$engine&searchType=image&filter=1&num=1&fields=items%2Flink"

  // Add terms that are unique to each search, except for KEY- needs to be appended at end!
  private def searchUrl(encodedTerms: String, isGif: Boolean, safety: String, start: Int): String =
    baseSearchUrl + s"&q=cute+$encodedTerms${if (isGif) "&type=gif" else ""}&safe=$safety&start=$start&key="


  def search(terms: String, safetyLevel: SafetyLevel): Future[String] = {
    val isGif = terms.toLowerCase.endsWith("gif")

    val encoded = Try(URLEncoder.encode(terms, "UTF-8").replaceAll("\\.","%2E")).get

    val startingPoint = Random.nextInt(100)

    val noKeyUrl = searchUrl(encoded, isGif, safetyLevel.name, startingPoint)


    import scala.concurrent.duration._

    TimerUtils.tryWithBackoff[String](100.millis,2.second)({
      Future {
        val url = noKeyUrl + nextKey()
        new URL(url).openStream().asString().filter(!Character.isWhitespace(_)) match {
          case gr""".*"link":"${link: String}(.+)".*""" => link
          case other =>
            Logger.error("Weird response from Google")("Response" -> other)
            throw CuteException("This search turned up no results!")
        }
      }
    }, {
      // Retry on a 40x response
      case Failure(e: IOException) if e.getMessage.contains("40") => true

    }).transform(identity, {
      // Forward cute exceptions
      case e: CuteException => e

      // If got a 40x response
      case e: IOException if e.getMessage.contains("40") =>
        CuteException("Probably reached daily limit :(")

      // If got a 50x response
      case e: IOException if e.getMessage.contains("50") =>
        CuteException("Google's servers are probably down. Try again in a minute.")

      // Otherwise log the error
      case e =>
        Logger.error(e)("URL" -> noKeyUrl)
        CuteException("Unhandled exception occurred. Check RedBot support server for more information.")
    })
  }
}