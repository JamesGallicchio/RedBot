package redbot.bots

import java.net.URL
import java.util.concurrent.Executors

import redbot.bots.FeedBot._
import redbot.cmd.Command
import redbot.discord.Permission.ManageChannels
import redbot.discord.Snowflake._
import redbot.discord._
import redbot.utils.{DataStore, InputUtils, JoinMap, Logger}
import regex.Grinch

import scala.concurrent.Future
import scala.util.{Success, Failure}
import scala.collection.mutable
import scala.collection.JavaConverters._

case class CrushBot(client: Client) extends CommandBot {
  private implicit val ec = scala.concurrent.ExecutionContext.global

  override def commands: Seq[Command[_]] = Vector(
    Command("crush <username#0000>",
      "Marks the specified user as one of your crushes") ( msg => {
        case gr"crush $user(.+)#$discrim(\d{4})" =>
          client.getChannel(msg.channelId).onComplete {
            case Success(c) if !c.isPM =>
              Future.sequence(
                (for {
                  g <- client.getGuilds
                  ms = client.getMembers(g)
                  if ms contains msg.user.id
                  uid <- ms
                } yield client.getUser(uid)).
                map(fu => fu.map(Success(_)).recover{
                  case e => Failure(e)
                })
              ).map(_
                .collect { case Success(u) => (u,
                  InputUtils.distance(u.username, user) +
                  InputUtils.distance(u.discrim, discrim))
                }
              ).foreach { found =>
                if (found.isEmpty)
                  msg.reply(s"User not found. Check that the username and discriminant are correct.")
                else {
                  val closest = found.minBy(_._2)
                  if (closest._2 != 0) {
                    msg.reply(s"User not found. Closest match: ${closest._1.username}#${closest._1.discrim}")
                  } else {
                    val u = closest._1
    
                    val s = crushes.getOrElseUpdate(msg.user.id, Set.empty)
                    crushes(msg.user.id) = s + u.id
    
                    if (crushes.getOrElseUpdate(u.id, Set.empty) contains msg.user.id) {
                      client.getPM(u.id).foreach { c =>
                        client.sendMessage(c, s"You matched with ${msg.user.toString}!")
                      }
                      msg.reply("It's a match! I've sent a message their way.")
                    } else {
                      msg.reply("Crush noted! You'll get a message if you match.")
                    }
                    saveCrushes()
                  }
                }
              }
          }
      }
  ))

  import CrushBot._
  private val crushes: mutable.Map[User.Id, Set[User.Id]] =
    DataStore.getOrElse("crush_map", mutable.Map.empty[User.Id, Set[User.Id]])
  private def saveCrushes(): Unit = DataStore.store("crush_map", crushes)
}

object CrushBot {
  import play.api.libs.json._

  private implicit val userIdFormat: Format[User.Id] = Format(
    Reads.LongReads.map(_.asId[User.Id]),
    Writes.LongWrites
  )

  private implicit val crushesFormat: Format[mutable.Map[User.Id, Set[User.Id]]] = Format(
    Reads.ArrayReads[(User.Id, Set[User.Id])].map(a => mutable.Map(a.toSeq: _*)),
    Writes.arrayWrites[(User.Id, Set[User.Id])].contramap(m => m.toArray)
  )
}
