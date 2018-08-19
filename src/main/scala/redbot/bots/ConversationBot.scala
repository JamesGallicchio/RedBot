package redbot.bots

import redbot.cmd.Command
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
trait ConversationBot extends CommandBot {

  // Map from channel and user snowflakes (XOR'd) to conversation state machines
  private val convos = mutable.LongMap.empty[StateMachine[Command]]

  override def handler: PartialFunction[Command, Unit] = {case cmd @ Command(args, msg, cli) =>
    // Map key
    val key = msg.channel ^ cmd.user

    // State machine for the conversation
    val sm = convos.remove(key).getOrElse(newConvo)

    // Throw the command in
    sm(cmd)

    // Put the state machine back
    convos.put(key, sm)
  }

  def newConvo: StateMachine[Command]
}