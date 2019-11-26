package redbot.discord.impl.d4j

import java.util.Optional

import discord4j.core.`object`.util.Snowflake
import reactor.core.publisher
import reactor.core.scala.publisher.{PimpMyPublisher, SFlux, SMono}
import redbot.utils.Logger

import scala.concurrent.{Future, Promise}

object JavaConversions {
  implicit class Optj2s[T >: Null](val op: Optional[T]) extends AnyVal {
    def toScala: Option[T] = Option(op.orElse(null))
  }
  implicit class Opts2j[T >: Null](val op: Option[T]) extends AnyVal {
    def toJava: Optional[T] = Optional.ofNullable(op.orNull)
  }
  implicit class FluxJ2S[T](val flux: publisher.Flux[T]) extends AnyVal {
    def asScala: SFlux[T] = PimpMyPublisher.jFlux2SFlux(flux)
  }
  implicit class MonoJ2S[T](val mono: publisher.Mono[T]) extends AnyVal {
    def asScala: SMono[T] = PimpMyPublisher.jMono2SMono(mono)
  }

  implicit class MonoS2Future[T](val mono: SMono[T]) extends AnyVal {
    def asFuture: Future[T] = {
      Logger.debug("Building future from mono")
      val p = Promise[T]
      mono.subscribe({ x =>
        Logger.debug(s"Completing promise with value $x")
        p success x
      }, { e =>
        Logger.debug(s"Completing promise with error $e")
        p failure e
      })
      p.future
    }
  }
}

object ReactorMonadics {
  implicit class FluxMonadic[T](val f: SFlux[T]) extends AnyVal {
    def withFilter(pred: T => Boolean): SFlux[T] = f.filter(pred)
    def foreach[U](func: T => U): Unit = f.map(func).subscribe()
  }
  implicit class MonoMonadic[T](val m: SMono[T]) extends AnyVal {
    def withFilter(pred: T => Boolean): SMono[T] = m.filter(pred)
    def foreach[U](func: T => U): Unit = m.subscribe(t => func(t))
  }
}

object SnowflakeConversions {
  implicit class Snowflake2Mine(val s: Snowflake) extends AnyVal {
    def as[T <: redbot.discord.Snowflake.Snowflake]: T = s.asLong.asInstanceOf[T]
  }
}