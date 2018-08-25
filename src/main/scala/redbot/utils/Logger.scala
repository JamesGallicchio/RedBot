package redbot.utils

object Logger { //TODO: entire class
  def log(message: String)(context: (String, Any)*): Unit = {
    val name = new Exception().getStackTrace.slice(3, 4).map(caller => caller.getFileName + ":" + caller.getLineNumber).mkString("\n")
  }
  def log(exception: Throwable)(context: (String, Any)*): Unit = ???

  private def handleLog(text: String) = ???
}
