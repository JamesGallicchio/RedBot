package redbot.bots

import java.net.URL

import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import redbot.cmd.Command
import redbot.discord.Permission.ManageChannels
import redbot.discord.{Channel, Client}
import regex.Grinch

import scala.collection.concurrent.TrieMap
import scala.util.{Failure, Success, Try}
import scala.collection.JavaConverters._
import FeedBot._
import redbot.discord.Channel.Id
import redbot.utils.InputUtils

case class FeedBot(client: Client) extends CommandBot {

  // URL hash to Feed
  val subs = new TrieMap[Int, Subscription]()

  private def replaceByTerms(terms: String, func: Subscription => Subscription): Option[Subscription] =
    // Handle as if is a URL
    Try(new URL(terms)).toOption.flatMap { url =>

      // Handle as if is an existing URL
      subs.get(url.##) orElse {

        // Handle as if is a new URL
        Subscription.ofUrl(url).toOption.map { sub =>
          subs.getOrElseUpdate(terms.##, sub)
        }
      }
    } orElse {

      // Handle as if is an existing ID
      for {
        id <- (terms: String).toHash
        feed <- subs.get(id)
      } yield feed
    } orElse {

      // Handle as if is an existing name

      // Sort by string comparison certainty
      val sorted = subs.values.map(sub => (sub, InputUtils.certainty(terms, sub.last.getTitle))).toSeq
        .sorted(Ordering.by[(Subscription, Int), Int](_._2).reverse)

      sorted.length match {
        case 1 if sorted.head._2 > 2 => // Ensure certainty > 2
          Some(sorted.head._1)
        case x if x > 1 && sorted(0)._2 - sorted(1)._2 > 3 => // Ensure highest certainty is > 3 more than second
          Some(sorted.head._1)
        case _ => None
      }
    } match { // Repeat atomic op until succeeds

      case Some(sub) =>
        // Replace subscription with new one that has new channel sub'd (atomic op in case subs changed in between)
        if (subs.replace(sub.##, sub, func(sub)))
          Some(sub)
        else None

      case None => throw new NoSuchElementException
    }

  def search(terms: String): Seq[Subscription] = {
    subs.values.toSeq
      .sorted(Ordering.by[Subscription, Int]{ _.channels.size }(Ordering.Int.reverse))
      .sorted(Ordering.by[Subscription, Int]{ sub => InputUtils.certainty(sub.last.getTitle, terms)} (Ordering.Int.reverse) )
  }

  override def commands: Seq[Command[_]] = Vector(
    Command("subscribe <URL | ID | NAME>",
      "Subscribes the current channel to the given URL, or to the feed matching NAME or ID in the catalog.")(msg => {
      case gr"subscribe (.+)${terms: String}" => msg.checkPerms(ManageChannels){

        while (
          Try(replaceByTerms(terms, _.replaceChannels(_ + msg.channelId))) match {
            case Success(None) => true // Retry
            case Success(Some(sub)) => msg.reply(s"Subscribed to ${sub.last.getTitle}"); false // Exit loop
            case Failure(_) =>
              msg.reply("Unable to subscribe. Either URL was invalid, ID was not found, or NAME didn't match any existing subs close enough.")
              false // Exit loop
          }
        ) {}
      }
    }),
    Command("unsubscribe <URL | ID | NAME>",
      "Unsubscribes the current channel to the given URL, or to the feed matching NAME or ID in the catalog.")(msg => {
      case gr"unsubscribe (.+)${terms: String}" => msg.checkPerms(ManageChannels) {

        while (
          Try(replaceByTerms(terms, _.replaceChannels(_ + msg.channelId))) match {
            case Success(None) => true // Retry
            case Success(Some(sub)) => msg.reply(s"Unsubscribed from ${sub.last.getTitle}"); false // Exit loop
            case Failure(_) =>
              msg.reply("Unable to unsubscribe. Either URL was invalid, ID was not found, or NAME didn't match any existing subs close enough.")
              false // Exit loop
          }
        ) {}
      }
    }),
    Command("subs | subscriptions",
      "Lists subscriptions in the current channel.")(msg => {
      case "subs" | "subscriptions" =>
        msg.reply(subs.values.filter(_.channels.contains(msg.channelId)).mkString("\n"))
    }),
    Command("catalog [PAGE] [SEARCH TERMS]",
      "Lists all feeds the bot is aware of, 10 per page. The listing is ordered by the number of subscribed channels, " +
      "or by the relevance to the search terms, if provided.")(msg => {
      case gr"catalog (\\d+)${page: Int} (.+)${terms: String}" =>
        if (page < 1) msg.reply("Page number must be 1 or higher!")
        else
          msg.reply(search(terms).slice((page-1) * 10, page * 10).mkString("\n"))

      case gr"catalog (.+)${terms: String}" =>
        msg.reply(search(terms).take(10).mkString("\n"))
    })
  )
}

object FeedBot {
  private val io = new SyndFeedInput()

  case class Subscription(url: URL, channels: Set[Channel.Id], last: SyndFeed) {

    def updated: Try[(Subscription, Seq[SyndEntry])] =
      getFeed(url).map { feed =>

        val sub = Subscription(url, channels, feed)
        val entryDiff = feed.getEntries.asScala.filter(last.getEntries.contains)
        (sub, entryDiff)
      }

    override lazy val toString: String = s"*${url.##.toHex}* - ${last.getTitle} [${channels.size} channels]\n    ($url)"

    def replaceChannels(func: Set[Channel.Id] => Set[Channel.Id]): Subscription =
      Subscription(url, func(channels), last)

    override def hashCode(): Int = url.hashCode()
    override def equals(o: scala.Any): Boolean = o match {
      case Subscription(other, _, _) => url == other
      case _ => false
    }
  }
  object Subscription {
    def ofUrl(url: URL): Try[Subscription] =
      getFeed(url).map(new Subscription(url, Set.empty, _))
  }

  private def getFeed(url: URL): Try[SyndFeed] = Try(io.build(new XmlReader(url)))

  private[FeedBot] implicit class Hash2Hex(val int: Int) extends AnyVal {
    def toHex: String = java.lang.Integer.toUnsignedString(int, 16)
  }
  private[FeedBot] implicit class Hex2Hash(val str: String) extends AnyVal {
    def toHash: Option[Int] = Try(java.lang.Integer.parseUnsignedInt(str, 16)).toOption
  }
}