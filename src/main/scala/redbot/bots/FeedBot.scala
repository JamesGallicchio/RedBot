package redbot.bots

import java.net.URL
import java.util.concurrent.{Callable, Executors}

import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import com.sun.net.httpserver.Authenticator
import org.jsoup.Jsoup
import org.jsoup.nodes.{Element, Node, TextNode}
import play.api.libs.json._
import redbot.bots.FeedBot._
import redbot.cmd.Command
import redbot.discord.Permission.ManageChannels
import redbot.discord._
import redbot.utils.{DataStore, InputUtils, Logger}
import regex.Grinch

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap
import scala.util.{Failure, Success, Try}

case class FeedBot(client: Client) extends CommandBot {

  override def commands: Seq[Command[_]] = Vector(
    Command("subscribe <URL | ID | NAME>",
      "Subscribes the current channel to the given URL, or to the feed matching NAME or ID in the catalog.")(msg => {
      case gr"subscribe $terms(.+)" => msg.checkPerms(ManageChannels){

        while (
          Try(replaceByTerms(terms, _.replaceChannels(_ + msg.channelId))) match {
            case Success(None) => true // Retry
            case Success(Some(sub)) => msg.reply(s"Subscribed to ${sub.feed.getTitle}"); false // Exit loop
            case Failure(_) =>
              msg.reply("Unable to subscribe. Either URL was invalid, ID was not found, or NAME didn't match any existing subs close enough.")
              false // Exit loop
          }
        ) {}
      }
    }),
    Command("unsubscribe <URL | ID | NAME>",
      "Unsubscribes the current channel to the given URL, or to the feed matching NAME or ID in the catalog.")(msg => {
      case gr"unsubscribe $terms(.+)" => msg.checkPerms(ManageChannels) {

        while (
          Try(replaceByTerms(terms, _.replaceChannels(_ - msg.channelId))) match {
            case Success(None) => true // Retry
            case Success(Some(sub)) => msg.reply(s"Unsubscribed from ${sub.feed.getTitle}"); false // Exit loop
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
      case gr"catalog ${pageStr: String}(\d+)${terms: String}(.*)" =>
        Try(pageStr.toInt) map { page =>
          if (page < 1 || (page-1)*10 >= subs.size)
            msg.reply(s"Page number must be between 1 and ${subs.size/10 + 1}!")
          else
            msg.reply(orderByTerms(terms.trim).slice((page - 1) * 10, page * 10).mkString("\n"))
        } getOrElse msg.reply("Invalid page number.")

      case gr"catalog${terms: String}(.*)" =>
        msg.reply(orderByTerms(terms.trim).take(10).map("\n" + _).mkString)
    })
  )

  import DataStore.Implicits._
  import redbot.discord.Snowflake._

  private implicit val urlFormat: Format[URL] = Format(
    Reads.StringReads.map(new URL(_)),
    (o: URL) => Writes.StringWrites.writes(o.toString))

  private implicit val setFormat: Format[Set[Channel.Id]] = Format(
    Reads.set(Reads.LongReads.map(_.asId[Channel.Id])),
    Writes.set)

  private implicit val subFormat: Format[Subscription] = Format(
    (json: JsValue) => (for {
      url <- (json \ "url").asOpt[URL]
      channels <- (json \ "channels").asOpt[Set[Channel.Id]]
      feed <- Subscription.getFeed(url).toOption
    } yield Subscription(url, channels, feed)).toJsResult(errMsg = "Unable to deserialize subscription"),

    (o: Subscription) => Json.obj(
      "url" -> o.url,
      "channels" -> o.channels
    ))

  // Map[URL hash -> Feed]
  private val subsIdent = "feed_subs"
  private type subsType = TrieMap[Int, Subscription]

  val subs: subsType = DataStore.get[subsType](subsIdent).getOrElse(new subsType())
  private def saveSubs(): Unit = DataStore.store(subsIdent, subs)

  private def replaceIfExists(key: Int, func: Subscription => Subscription): Unit = {
    while (! {
      subs.get(key).exists { sub =>
        subs.replace(key, sub, func(sub))
      }
    }) {}
    saveSubs()
  }

  private def replaceByTerms(terms: String, func: Subscription => Subscription): Option[Subscription] =
    findByTerms(terms) match {

      case Some(sub) =>
        // Replace subscription with new one that has new channel sub'd (atomic op in case subs changed in between)
        if (subs.replace(sub.##, sub, func(sub))) {
          saveSubs()
          Some(sub) // Successful (don't retry)
        } else None // Unsuccessful inserting into subs (need to retry)

      case _ => throw new IllegalArgumentException // Unable to handle terms
    }

  private def findByTerms(terms: String) =
    { // Handle as if is a URL
      for {
        url <- Try(new URL(terms)).toOption
        sub <- Subscription.ofUrl(url).toOption
      } yield subs.getOrElseUpdate(url.##, sub)

    } orElse { // Handle as if is an existing ID
      for {
        id <- terms.toHash
        feed <- subs.get(id)
      } yield feed

    } orElse { // Handle as if is an existing name

      // Sort by string comparison certainty
      val sorted = subs.values.map(sub => (sub, InputUtils.certainty(terms, sub.feed.getTitle))).toSeq
        .sorted(Ordering.by[(Subscription, Int), Int](_._2).reverse)

      sorted.length match {
        case 1 if sorted.head._2 > 2 => // Ensure certainty > 2
          Some(sorted.head._1)
        case x if x > 1 && sorted(0)._2 - sorted(1)._2 > 3 => // Ensure highest certainty is > 3 more than second
          Some(sorted.head._1)
        case _ => None
      }

    }

  private def orderByChannelCount: Seq[Subscription] =
    subs.values.toSeq
      .sorted(Ordering.by[Subscription, Int]{ _.channels.size }(Ordering.Int.reverse))

  private def orderByTerms(terms: String): Seq[Subscription] =
    orderByChannelCount.sorted(Ordering.by[Subscription, Int]{ sub =>
      InputUtils.certainty(sub.feed.getTitle, terms)
    } (Ordering.Int.reverse) )



  { // Every 2 seconds, update all the subs
    println("Setting up SUB TIMERS")
    import scala.concurrent.duration._
    val UPDATE_PERIOD = 2.seconds

    val exec = Executors.newScheduledThreadPool(8)
    exec.scheduleAtFixedRate(() => {
      println("Executing 2 min timer")
      val pause = (UPDATE_PERIOD/subs.size).toMillis

      subs.foldLeft(0L){ case (wait, (key, sub)) =>
        println(s"Scheduling an update in $wait millis for ${sub.url}")
        exec.schedule(() => {
          println(s"Updating ${sub.url}")
          sub.updated collect {
            case (feed, entries) =>
              // Send out new entries to all the subscribed channels

              sub.channels.foreach(client.sendEmbed(_, makeEmbed(feed, entries)))

              replaceIfExists(key, _.copy(feed = feed))
          }
        }, wait, MILLISECONDS)

        wait + pause
      }
    }, 0, UPDATE_PERIOD.toMillis, MILLISECONDS)
  }
}

object FeedBot {
  private val io = new SyndFeedInput()

  case class Subscription(url: URL, channels: Set[Channel.Id], feed: SyndFeed) {
    import Subscription._

    def updated: Try[(SyndFeed, Seq[SyndEntry])] =
      getFeed(url).map { feed =>
        (feed, feed.getEntries.asScala.filter(feed.getEntries.contains))
      }

    override lazy val toString: String = s"[${url.##.toHex}] **${feed.getTitle}** - *${channels.size}*\n    $url"

    def replaceChannels(func: Set[Channel.Id] => Set[Channel.Id]): Subscription =
      Subscription(url, func(channels), feed)

    def copy(url: URL = this.url, channels: Set[Channel.Id] = this.channels, feed: SyndFeed = this.feed) =
      Subscription(url, channels, feed)

    override def hashCode(): Int = url.hashCode()
    override def equals(o: scala.Any): Boolean = o match {
      case Subscription(other, _, _) => url == other
      case _ => false
    }
  }
  object Subscription {
    def ofUrl(url: URL): Try[Subscription] =
      getFeed(url).map(new Subscription(url, Set.empty, _))

    def getFeed(url: URL): Try[SyndFeed] =
      Try(io.build(new XmlReader(url))).recoverWith {
        case e => Logger.log(e)("URL" -> url); Failure(e) // Log errors getting URL
      }
  }


  def makeEmbed(feed: SyndFeed, entries: Seq[SyndEntry]): Embed = {
    import redbot.utils.OptParams._

    def findImage(node: Node): Option[(String, Option[String])] = node match {
      case e: Element if e.tagName == "img" =>
        e.attr("src").checkEmpty map {
          (_, e.attr("title").checkEmpty orElse e.attr("alt").checkEmpty)
        }

      case n if n.childNodes.size > 0 =>
        n.childNodes.asScala.map(findImage).reverse.reduceLeft { _ orElse _ }

      case _ => None
    }

    def discordify(node: Node): String = node match {
      case t: TextNode => t.text
      case e: Element =>
        val inner = e.childNodes.asScala.map(discordify).mkString.checkEmpty
        e.tagName match {
          case "a" => e.attr("href").checkEmpty.flatMap { url =>
            inner.map { c => s"[$c]($url)" }
          }.getOrElse("")

          case "b" => inner.map { c => s"**$c**" }.getOrElse("")
          case "u" => inner.map { c => s"__${c}__" }.getOrElse("")
          case "i" => inner.map { c => s"*$c*" }.getOrElse("")

          case "h1" | "h2" | "h3" | "h4" | "h5" =>
            inner.map { c => s"\n\n**$c**\n" }.getOrElse("")

          case "p" => "\n\n"
          case "br" => "\n"

          case _ => inner.getOrElse("")
        }
      case n => n.childNodes.asScala.map(discordify).mkString
    }


    def limitLinked(text: String, link: String): String = {
      val limit = 1024

      if (text.isEmpty)
        s"[more]($link)"
      else if (text.length + link.length + 4 > limit)
        s"${text.take(limit - link.length - 12)}... [more]($link)"
      else
        s"[$text]($link)"
    }

    val base = Embed(
      author = EmbedAuthor(feed.getTitle),
      thumbnailUrl = feed.getImage? (_.getUrl)
    )

    if (entries.size > 1) {
      val htmls = entries.map(e => (e, Jsoup.parseBodyFragment(e.getDescription.getValue)))

      val img = htmls.map(_._2).map(findImage).reduceLeft { _ orElse _ }

      base copy (
        fields = htmls.map { case (entry, html) =>
          EmbedField(entry.getTitle, limitLinked(discordify(html), entry.getLink), inline = true)
        },
        imageUrl = img map(_._1),
        footer = img flatMap(_._2) map(EmbedFooter(_))
      )
    } else {
      val e = entries.head
      val html = Jsoup.parseBodyFragment(e.getDescription.getValue)
      val img = findImage(html)

      base copy(
        title = e.getTitle,
        description = limitLinked(e.getDescription.getValue.checkEmpty.getOrElse("more"), e.getLink),
        imageUrl = img map(_._1),
        footer = img flatMap(_._2) map(EmbedFooter(_)),
        timestamp = e.getPublishedDate.toInstant
      )
    }
  }


  implicit class Hash2Hex(val int: Int) extends AnyVal {
    def toHex: String = java.lang.Integer.toUnsignedString(int, 16)
  }
  implicit class Hex2Hash(val str: String) extends AnyVal {
    def toHash: Option[Int] = Try(java.lang.Integer.parseUnsignedInt(str, 16)).toOption
  }
  implicit class Str2Option(val str: String) extends AnyVal {
    def checkEmpty: Option[String] = if (str.isEmpty) None else Some(str)
  }
}