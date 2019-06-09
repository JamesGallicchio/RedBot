package redbot.utils

import org.slf4j.LoggerFactory

object Logger {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def log(message: String)(context: (String, Any)*): Unit =
    logger.error(
      message + "\n" + format(context) + "\n\n" + caller()
    )

  def log(exception: Throwable)(context: (String, Any)*): Unit =
    logger.error(
      exception.getMessage + "\n" + format(context) + "\n\n" + caller(), exception
    )

  private def format(context: Seq[(String,Any)]): String = {
    context.map{case(name, obj) => s"$name := $obj"}.mkString("\n")
  }

  private def caller(): String = {
    new Exception().getStackTrace.drop(2).map{ c =>
      s"${c.getClassName}.${c.getMethodName}(${c.getFileName}:${c.getLineNumber})"
    }.mkString("\n    at ")
  }
}
