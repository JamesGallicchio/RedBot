package redbot.bots

import redbot.GlobalRefs
import redbot.cmd.{Command, CommandMessage}
import redbot.discord.User
import redbot.utils.Logger

import scala.util.{Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global

abstract class CommandBot extends DiscordBot {
  def commands: Seq[Command]

  lazy val help: String = commands.map{ cmd => s"${cmd.format}\n    ${cmd.description}" }
    .foldLeft(new StringBuilder("Commands: \n```\n"))(_ ++= _ ++= "\n").append("```").toString

  lazy val helpCommand: Command =
    Command("help", "Lists commands this bot includes")(cmd => {
      case "help" => cmd.reply(help)
    })

  lazy val allCommands: Seq[Command] = commands :+ helpCommand

  lazy val undefinedResponse: String =
    s"""Use '$prefix help' to get a list of commands you can use. Commands are case-sensitive!
       |Join the RedBot support server to see what other bots are available: ${GlobalRefs.ServerInvite}
    """.stripMargin

  def handle(msg: CommandMessage): Unit =
    allCommands.map(_.action(msg)) // Get the action for the command
      .filter { _.isDefinedAt(msg.content) } // Make sure the action is defined
      .map { _.apply(msg.content) } // Give the action the message content
      .headOption.getOrElse(msg.reply(undefinedResponse)) // If no commands matched, respond

  lazy val prefix: String = User.mention(client.getSelfId)

  client.addMessageListener { msg =>
    CommandMessage(client, msg, prefix).onComplete {
      case Success(Some(cmsg)) => handle(cmsg)
      case Success(None) => // Ignore that message

      case Failure(e) =>
        Logger.log(e)("Channel ID" -> msg.channel, "Message ID" -> msg.id)
    }
  }
}