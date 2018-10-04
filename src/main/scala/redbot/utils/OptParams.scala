package redbot.utils

object OptParams {
  trait OptParamTag
  type ?[+T] = Option[T] with OptParamTag

  implicit def Opt2Param[T]: Option[T] => ?[T] = _.asInstanceOf[?[T]]
  implicit def OptConfusion[T]: Option[T] => Option[T] = throw new IllegalStateException("This shouldn't be callable...")
  implicit def Val2Param[T]: T => ?[T] = Option(_).asInstanceOf[?[T]]
  implicit def ValConfusion[T]: T => Option[T] = throw new IllegalStateException("This shouldn't be callable...")

  implicit class Nullable2Opt[T <: AnyRef](val nullable: T) extends AnyVal {
    def ? : Option[T] = Option(nullable)
  }

  implicit class ZipOpts[T1](val opt: Option[T1]) extends AnyVal {
    def zip[T2](otherOpt: Option[T2]): Option[(T1, T2)] = (opt, otherOpt) match {
      case (Some(t1), Some(t2)) => Some((t1, t2))
      case _ => None
    }
  }
}
