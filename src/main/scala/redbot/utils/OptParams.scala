package redbot.utils

object OptParams {
  trait OptParamTag
  type ?[+T] = Option[T] with OptParamTag
  val ? = Option.empty[Nothing]

  implicit def Opt2Param[T]: Option[T] => ?[T] = _.asInstanceOf[?[T]]
  implicit def OptConfusion[T]: Option[T] => Option[T] = throw new IllegalStateException("This shouldn't be callable...")
  implicit def Val2Param[T]: T => ?[T] = Option(_).asInstanceOf[?[T]]
  implicit def ValConfusion[T]: T => Option[T] = throw new IllegalStateException("This shouldn't be callable...")

  implicit class Nullable2Opt[T <: AnyRef](val nullable: T) extends AnyVal {
    def ? : Option[T] = Option(nullable)
    def ?[R](mapper: T => R): Option[R] = Option(nullable) map mapper
  }
}
