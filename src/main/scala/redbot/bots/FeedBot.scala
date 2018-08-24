package redbot.bots

import redbot.cmd.CommandMessage
import redbot.discord.Client
import redbot.utils.StateMachine

case class FeedBot(client: Client) extends ConversationBot {

  val helpText: String =
    """
      |subscribe <URL | NAME>
      |   Subscribes the current channel to the given URL,
      |   or to the feed matching NAME in the catalog.
      |
      |unsubscribe <URL | NAME>
      |   Unsubscribes the current channel to the given feed.
      |   Use the `sub` command to
      |
      |subs | subscriptions
      |   Lists subscriptions in current channel.
      |
      |catalog
      |   Lists all feeds the bot is aware of, ordered by
      |   number of subscribed channels.
      |
     """.stripMargin

  override def newConvo: StateMachine[CommandMessage] = ???
}