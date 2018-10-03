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

  def ?[T1 <: AnyRef, T2 <: AnyRef](t1: T1, t2: T2): Option[(T1, T2)] =
    if (t1 == null || t2 == null) None else Some((t1, t2))

  def ?[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef](t1: T1, t2: T2, t3: T3): Option[(T1, T2, T3)] =
    if (t1 == null || t2 == null || t3 == null) None else Some((t1, t2, t3))
}
