package redbot.utils

import java.util.concurrent.Executors

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object TimerUtils {
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  private val isFailureDefault: PartialFunction[Try[_], Boolean] = {case x => x.isFailure}

  def tryWithBackoff[T](initialBackoff: FiniteDuration,
                        maxBackoff: Duration,
                        backoffIncrement: Long => Long = _*2)
                       (operation: => Future[T],
                        isFailure: PartialFunction[Try[T], Boolean] = isFailureDefault): Future[T] =
    operation.andThen {
      case res if isFailure.isDefinedAt(res) && isFailure(res) =>
        case _ if maxBackoff.toMillis > initialBackoff.toMillis =>
          val next = backoffIncrement(initialBackoff.toMillis)

          timer(next.millis).flatMap(_ =>
            tryWithBackoff(next.millis, maxBackoff, backoffIncrement)(operation,isFailure))
      case res => Future.fromTry(res)
    }

  def timer(at: FiniteDuration): Future[Unit] = {
    val res = Promise[Unit]()
    scheduler.schedule(() => res.success(()), at.length, at.unit)
    res.future
  }
}
