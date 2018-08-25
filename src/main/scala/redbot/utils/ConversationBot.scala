package redbot.bots

import redbot.cmd.CommandMessage
import redbot.utils.StateMachine

import scala.collection.mutable

/**
  * Superbot to handle "conversations"- messages back and forth between user
  * and bot, handled by state machine transitions.
  *
  * Conversations are unique to a channel and user. A user can only have one
  * conversation at a time in a certain channel, but can have concurrent
  * conversations in different channels.
  *
  * @author JamesGallicchio
  */
trait ConversationBot {

  // Map from channel and user snowflakes (XOR'd) to conversation state machines
  private val convos = mutable.LongMap.empty[StateMachine[CommandMessage]]

  def handler: PartialFunction[CommandMessage, Unit] = {case msg =>
    // Map key
    val key = msg.channelId ^ msg.user.id

    // State machine for the conversation
    val sm = convos.remove(key).getOrElse(newConvo)

    // Throw the command in
    sm(msg)

    // Put the state machine back
    convos.put(key, sm)
  }

  def newConvo: StateMachine[CommandMessage]
}