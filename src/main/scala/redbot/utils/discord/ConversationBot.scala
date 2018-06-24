package redbot.utils.discord
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.{Event, message}
import reactor.core.scala.publisher.Flux
import redbot.utils.StateMachine

trait ConversationBot extends BaseBot {
  // Map from (TextChannel, User) snowflakes to
  val convos = scala.collection.mutable.Map.empty[(Snowflake, Snowflake), StateMachine[Message]]

  override def consumeEvents(evFlux: Flux[Event]) =
    for {
      ev <- evFlux.flatMapIterable { case e: MessageCreateEvent => Some(e) ; case _ => None }

    } convos

  def newConversation: StateMachine[Message]
}
