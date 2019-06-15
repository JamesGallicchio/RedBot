package redbot.bots

import java.net.URL
import java.util.concurrent.Executors

import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Element, Node, TextNode}
import redbot.bots.FeedBot._
import redbot.cmd.Command
import redbot.discord.Permission.ManageChannels
import redbot.discord.Snowflake._
import redbot.discord._
import redbot.utils.{DataStore, InputUtils, JoinMap, Logger}
import regex.Grinch

import scala.jdk.CollectionConverters._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

case class FeedBot(client: Client) extends CommandBot {

  override def commands: Seq[Command[_]] = Vector(
    Command("subscribe <URL | ID | NAME>",
      "Subscribes the current channel to the given URL, or to the feed matching NAME or ID in the catalog.")(msg => {
      case gr"subscribe $terms(.+)" => msg.checkPerms(ManageChannels){

        findKeyByTerms(terms) match {
          case None =>
            msg.reply("Unable to subscribe. Either URL was wrong, ID was not found, " +
                                          "or NAME didn't match any existing subs well enough.")
          case Some(key) =>
            subscribe(key, msg.channelId) match {
              case None => msg.reply("This channel is already subscribed to that feed.")
              case Some(Success(f)) => msg.reply(s"Subscribed to feed `${f.title.getOrEmpty}`")
              case Some(Failure(_)) => msg.reply("No feed found at that URL, or feed currently unavailable.")
            }
        }
      }
    }),
    Command("unsubscribe <URL | ID | NAME>",
      "Unsubscribes the current channel to the given URL, or to the feed matching NAME or ID in the catalog.")(msg => {
      case gr"unsubscribe $terms(.+)" => msg.checkPerms(ManageChannels) {

        findKeyByTerms(terms) match {
          case None => msg.reply("Unable to subscribe. Either URL was wrong, ID was not found, " +
                                          "or NAME didn't match any existing subs well enough.")
          case Some(key) =>
            unsubscribe(key, msg.channelId) match {
              case None => msg.reply("This channel was not yet subscribed to that feed.")
              case Some(f) => msg.reply(s"Unsubscribed from `${f.title.getOrEmpty}`")
            }
        }
      }
    }),
    Command("subs | subscriptions",
      "Lists subscriptions in the current channel.")(msg => {
      case "subs" | "subscriptions" =>
        msg.reply( (for {
          url <- subs.getR(msg.channelId).getOrElse(Set.empty)
          rf <- feeds.get(url)
        } yield formatFeed(url, rf)).mkString("\n", "\n", ""))
    }),
    Command("catalog [PAGE] [SEARCH TERMS]",
      "Lists all feeds the bot is aware of, 10 per page. The listing is ordered by the number of subscribed channels, " +
        "or by the relevance to the search terms, if provided.")(msg => {
      case gr"catalog ${pageStr: String}(\d+)${terms: String}(.*)" =>
        Try(pageStr.toInt) map { page =>
          if (page < 1 || (page-1)*10 >= feeds.size)
            msg.reply(s"Page number must be between 1 and ${feeds.size/10 + 1}.")
          else
            msg.reply(orderByTerms(terms.trim).slice((page - 1) * 10, page * 10).
              map { case (url, rf) => formatFeed(url, rf)}.mkString("\n","\n",""))
        } getOrElse msg.reply("Invalid page number.")

      case gr"catalog${terms: String}(.*)" =>
        msg.reply(orderByTerms(terms.trim).take(10).
          map { case (url, rf) => formatFeed(url, rf)}.mkString("\n","\n",""))
    })
  )

  // JoinMap from URL strings to channel IDs
  import FeedBot.{channelIdFormat, reducedFeedFormat}
  import JoinMap.LeftKeyedFormat
  private val subs: JoinMap[String, Channel.Id] = DataStore.getOrElse("feed_subs", JoinMap.empty[String, Channel.Id])
  private def saveSubs(): Unit = DataStore.store("feed_subs", subs)(LeftKeyedFormat[String, Channel.Id])

  // Map from URL string to most recent copy of the feed
  private val feeds: mutable.Map[String, ReducedFeed] = mutable.Map(DataStore.getOrElse("feed_cache", Map.empty[String, ReducedFeed]).toSeq:_*)
  private def saveFeeds(): Unit = DataStore.store("feed_cache", feeds)

  private def formatFeed(url: String, feed: ReducedFeed): String = {
    val channels = subs.getL(url).map(_.size).getOrElse(0)
    val hexHash = url.##.toHex
    val title = feed.title.getOrElse("")
    s"[$hexHash] **$title** - *$channels channels*\n    $url"
  }

  // Returns Some(Success(ReducedFeed)) if successfully subscribes, None if already subscribed, Failure if encountered error
  private def subscribe(url: String, channel: Channel.Id): Option[Try[ReducedFeed]] =
    if (subs.isJoined(url, channel)) None
    else Some(feeds.get(url) match {
      case Some(f) =>
        subs.join(url, channel)
        saveSubs()
        Success(f)
      case None => getFeed(url).map(ReducedFeed(_)).map { f =>
        feeds.update(url, f)
        subs.join(url, channel)
        saveFeeds()
        saveSubs()
        f
      }
    })

  // Returns Some(ReducedFeed) if successfully unsubscribes, None if wasn't subscribed
  private def unsubscribe(url: String, channel: Channel.Id): Option[ReducedFeed] =
    if (subs.isJoined(url, channel)) {
      subs.unjoin(url, channel)
      saveSubs()
      feeds.get(url)
    }
    else None


  private def findKeyByHash(hash: Int): Option[String] =
    feeds.keys.find(_.## == hash)
  private def findKeyByUrl(url: String): Option[String] =
    InputUtils.verifyURL(url).map(_.toString).toOption.
      filter { f => feeds.contains(f) || getFeed(f).isSuccess }
  private def findKeyByTitleFuzzy(title: String): Option[String] = if (feeds.isEmpty) None else {
    val closest = feeds.toSeq.map { case (u, f) => (u, InputUtils.closeness(f.title.getOrEmpty, title)) }.
                              maxBy(tuple => tuple._2)
    closest match {
      case (sub, c) if c > 5 => Some(sub)
      case _ => None
    }
  }
  private def findKeyByTerms(terms: String): Option[String] =
    terms.fromHex.flatMap(findKeyByHash) orElse
    findKeyByUrl(terms) orElse
    findKeyByTitleFuzzy(terms)

  private def orderByChannelCount: Seq[(String, ReducedFeed)] =
    feeds.toSeq.sorted(Ordering.by[(String, ReducedFeed), Int] {
      case (key, _) => subs.getL(key).map(_.size).getOrElse(0)
    }(Ordering.Int.reverse))
  private def orderByTerms(terms: String): Seq[(String, ReducedFeed)] =
    orderByChannelCount.sorted(Ordering.by[(String, ReducedFeed), Int]{ sub =>
      InputUtils.closeness(sub._2.title.getOrEmpty, terms)
    } (Ordering.Int.reverse) )



  { // Every 2 minutes, update all the feeds
    import scala.concurrent.duration._
    val UPDATE_PERIOD = 2.minutes

    val exec = Executors.newScheduledThreadPool(8)
    exec.scheduleAtFixedRate(() => {

      // Time between updating subs (distributes updates across entire UPDATE_PERIOD rather than all at once)
      val pause = (UPDATE_PERIOD/feeds.size).toMillis

      feeds.foldLeft(0L){ case (wait, (url, feed)) =>
        // Schedule to update this sub in $wait milliseconds
        exec.schedule((() => {
          // Get an updated feed
          getFeed(url) match {
            case Success(newFeed) =>
              // Calculate new entries
              val entries = newFeed.getEntries.asScala.toSeq.filterNot { e =>
                val reduced = ReducedEntry(e)
                feed.entries.contains(reduced)
              }
              // Send out new entries to all the subscribed channels
              val embed = makeEmbed(newFeed, entries)
              for (channels <- subs.getL(url))
                channels.foreach(client.sendEmbed(_, embed))

              feeds.update(url, ReducedFeed(newFeed))
              saveFeeds()

            case Failure(e) => Logger.error(e)("Sub" -> url) // Something went wrong updating the thread
          }
        }):Runnable, wait, MILLISECONDS)

        wait + pause
      }
    }, 0, UPDATE_PERIOD.toMillis, MILLISECONDS)
  }
}

object FeedBot {
  import redbot.utils.OptParams._
  private val io = new SyndFeedInput()

  private def getFeed(url: String): Try[SyndFeed] =
    Try(io.build(new XmlReader(new URL(url))))


  private case class ReducedEntry(link: Option[String], title: Option[String]) {
    def ==(o: ReducedEntry): Boolean =
      (for { l1 <- link; l2 <- o.link } yield l1 == l2) orElse
      (for { t1 <- title; t2 <- o.title } yield t1 == t2) getOrElse false
  }
  private object ReducedEntry {
    def apply(entry: SyndEntry): ReducedEntry = new ReducedEntry(entry.getLink.?, entry.getTitle.?)
  }

  private case class ReducedFeed(title: Option[String], entries: Seq[ReducedEntry])
  private object ReducedFeed {
    def apply(feed: SyndFeed): ReducedFeed = new ReducedFeed(
      feed.getTitle.?,
      feed.getEntries.asScala.toSeq.map(ReducedEntry(_)))
  }

  import play.api.libs.json._

  private implicit val reducedEntryFormat: Format[ReducedEntry] = Json.format
  private implicit val reducedFeedFormat: Format[ReducedFeed] = Json.format

  private implicit val urlFormat: Format[URL] = Format(
    Reads.StringReads.flatMap(s => Reads(_ => InputUtils.verifyURL(s) match {
      case Success(u) => JsSuccess(u)
      case Failure(_) => JsError("Unable to validate URL")
    })),
    Writes.StringWrites.contramap(_.toString)
  )

  private implicit val channelIdFormat: Format[Channel.Id] = Format(
    Reads.LongReads.map(_.asId[Channel.Id]),
    Writes.LongWrites
  )


  def makeEmbed(feed: SyndFeed, entries: Seq[SyndEntry]): Embed = {

    // Finds first <img> tag in the node structure
    def findImage(node: Node): Option[(String, Option[String])] = node match {
      case e: Element if e.tagName == "img" =>
        e.attr("src").checkEmpty map {
          (_, e.attr("title").checkEmpty orElse e.attr("alt").checkEmpty)
        }

      case n if n.childNodes.size > 0 =>
        n.childNodes.asScala.map(findImage).reduceLeft { _ orElse _ }

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

    // Links text to link using [text](link) and prevents entire result from exceeding limit
    def limitLinked(text: String, link: String): String = {
      val textTrim = text.trim
      val linkTrim = link.trim
      val limit = 1024

      if (textTrim.isEmpty)
        s"[more]($linkTrim)"
      else if (textTrim.length + linkTrim.length + 4 > limit)
        s"${textTrim.take(limit - linkTrim.length - 12)}... [more]($linkTrim)"
      else
        s"[$textTrim]($linkTrim)"
    }

    import redbot.utils.OptParams._
    val base: Embed = Embed(
      author = feed.getTitle.? map(EmbedAuthor(_)), // If feed title is nonnull, set it as embed author
      thumbnailUrl = feed.getImage.? map(_.getUrl) // If feed image is nonnull, set as thumbnail
    )

    // Multi entry format
    if (entries.size > 1) {
      val htmls = entries.map(e => (e, Jsoup.parseBodyFragment(e.getDescription.getValue)))

      // Get an image (if exists), and find first image from the updates
      val imgOpt = htmls.map(_._2).map(findImage).reduceLeft { _ orElse _ }

      base copy (
        fields = htmls.map { case (entry, html) =>
          EmbedField(entry.getTitle, limitLinked(discordify(html), entry.getLink), inline = true)
        },
        imageUrl = imgOpt.map(_._1),
        footer = imgOpt.flatMap(_._2) map(EmbedFooter(_))
      )
    } else { // Single entry format
      val e = entries.head
      val htmlOpt = e.getDescription.getValue.checkEmpty.map(Jsoup.parseBodyFragment) // Get desc html-parsed
      val imgOpt = htmlOpt.flatMap(findImage) // Find images in desc if they exist
      val desc = htmlOpt.map(discordify).flatMap(_.checkEmpty).getOrElse("more") // Make description discord-friendly, default to "more"

      base copy(
        title = e.getTitle,
        description = limitLinked(desc, e.getLink),
        imageUrl = imgOpt.map(_._1),
        footer = imgOpt.flatMap(_._2).map(EmbedFooter(_)),
        timestamp = e.getPublishedDate.toInstant
      )
    }
  }



  implicit class Str2Option(val str: String) extends AnyVal {
    def checkEmpty: Option[String] = if (str.isEmpty) None else Some(str)
  }

  implicit class Option2Str(val opt: Option[String]) extends AnyVal {
    def getOrEmpty: String = opt.getOrElse("")
  }



  implicit class Hash2Hex(val int: Int) extends AnyVal {
    def toHex: String = java.lang.Integer.toUnsignedString(int, 16)
  }
  implicit class Hex2Hash(val str: String) extends AnyVal {
    def fromHex: Option[Int] = Try(java.lang.Integer.parseUnsignedInt(str, 16)).toOption
  }


  implicit class Opt2JsRes[T](val o: Option[T]) extends AnyVal {
    def toJsResult(errMsg: String): JsResult[T] =
      o.map(JsSuccess(_)).getOrElse(JsError(errMsg))
  }
}