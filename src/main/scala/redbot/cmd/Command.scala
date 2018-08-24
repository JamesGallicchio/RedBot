package redbot.cmd

case class Command(format: String, description: String)(val action: CommandMessage => PartialFunction[String, Any])

