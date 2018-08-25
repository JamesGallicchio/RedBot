package redbot.utils

import java.util.concurrent.Executors

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object TimerUtils {
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  def tryWithBackoff[T](initialBackoff: FiniteDuration,
                        maxBackoff: Duration,
                        backoffIncrement: Long => Long = _*2,
                        accumulatedTime: Long = 0)
                       (operation: => Future[T]): Future[T] =
    operation.recoverWith {
      case ex if maxBackoff.toMillis > accumulatedTime =>
        val next = backoffIncrement(
          if (accumulatedTime == 0) initialBackoff.toMillis
          else accumulatedTime)

        timer(next.millis).flatMap(_ =>
          tryWithBackoff(initialBackoff, maxBackoff, backoffIncrement, next)(operation))
    }

  def timer(at: FiniteDuration): Future[Unit] = {
    val res = Promise[Unit]()
    scheduler.schedule(() => res.success(()), at.length, at.unit)
    res.future
  }
}
