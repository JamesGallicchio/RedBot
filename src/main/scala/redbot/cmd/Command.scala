package redbot.cmd

class Command[+R](val format: String, val description: String,
                  val action: CommandMessage => PartialFunction[String, R])
object Command {
  def apply[R](format: String, description: String)
              (action: CommandMessage => PartialFunction[String, R])
  : Command[R] = new Command[R](format, description, action)
}


