package redbot.utils

object OptParams {
  trait OptParamTag
  type ?[+T] = Option[T] with OptParamTag
  val ? = Option.empty[Nothing]

  implicit def Opt2Param[T]: Option[T] => ?[T] = _.asInstanceOf[?[T]]
  implicit def OptConfusion[T]: Option[T] => Option[T] = ???
  implicit def Val2Param[T]: T => ?[T] = Option(_).asInstanceOf[?[T]]
  implicit def ValConfusion[T]: T => Option[T] = ???
}
