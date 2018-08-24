package redbot.bots

import redbot.GlobalRefs
import redbot.cmd.{Command, CommandMessage}
import redbot.discord.User

abstract class CommandBot extends DiscordBot {
  def commands: Seq[Command]

  val help: String = commands.map{ cmd => s"${cmd.format} - ${cmd.description}" }
    .foldLeft(new StringBuilder("Commands: \n```\n"))(_ ++= _ ++= "\n").append("```").toString

  val helpCommand: Command =
    Command("help", "Lists commands this bot includes")(cmd => {
      case "help" => cmd.reply(help)
    })

  val cmdFunction: PartialFunction[CommandMessage, Any] = (commands :+ helpCommand)
    .map{cmd => Function.unlift[CommandMessage, Any](msg => cmd.action(msg).lift(msg.content))}
    .reduce(_.orElse(_))

  lazy val undefinedResponse: String =
    s"""Use "$prefix help" to get a list of commands you can use. Commands are case-sensitive!
       |Join the RedBot support server to see what other bots are available: ${GlobalRefs.ServerInvite}
    """.stripMargin

  def handle(msg: CommandMessage): Unit = cmdFunction.applyOrElse(msg, _.reply(undefinedResponse))

  lazy val prefix: String = User.mention(client.getSelfId)

  client.addMessageListener { msg =>
    CommandMessage(client, msg, prefix).foreach(handle)
  }
}