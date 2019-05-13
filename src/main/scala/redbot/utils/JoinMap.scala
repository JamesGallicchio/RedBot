package redbot.utils

import scala.collections.Map
import scala.collections.mutable.Map
import scala.collections.immutable.Set

class JoinMap[L, R] private (private val backingL: mutable.Map[L, Set[R]],
                             private val backingR: mutable.Map[R, Set[L]]) {
  def getL(key: L): Option[Set[R]] = backingL(key)
  def getR(key: R): Option[Set[L]] = backingR(key)
  
  def join(left: L, right: R): Unit = {
    backingL(left)  = backingL.getOrElse(left, new Set) + right
    backingR(right) = backingR.getOrElse(right, new Set) + left
  }
  
  def unjoin(left: L, right: R): Boolean =
    (for {
      rSet <- backingL(left)
      if rSet contains right
      lSet <- backingR(right)
      if lSet contains left
    } yield {
      backingL(left) = rSet - right
      backingR(right) = lSet - left
    }).isDefined
  
  def isJoined(left: L, right: R): Boolean =
    backingL(left).filter(_ contains right).isDefined
  
  def flip(): JoinMap[R, L] = new JoinMap(backingR, backingL)
  
  def clone(): JoinMap[L, R] = new JoinMap(backingL.clone(), backingR.clone())
  
  def keyedLeft(): Map[L, Set[R]] = backingL.clone()
  def keyedRight(): Map[R, Set[L]] = backingR.clone()
}

object JoinMap {
  def apply[L, R](): JoinMap[L, R] = new JoinMap(new mutable.Map, new mutable.Map)
  
  def apply[L, R](map: Map[L, R]): JoinMap[L, R] = {
    val j = JoinMap()
    for {
      (l, rSet) <- map
      r <- rSet
    } j.join(l, r)
    j
  }
  
  implicit def LeftKeyedFormat[A, B](implicit keyFormat: Format[A],
                                              valuesFormat: Format[Set[B]]): Format[JoinMap[A, B]] = {
    import play.api.libs.json.Reads._
    import play.api.libs.json.Writes._
    
    Format(
      Reads.ArrayReads[(A, Set[B])].map { case arr => JoinMap(arr.toMap) },
      Writes.arrayWrites[(A, Set[B])].contramap[JoinMap[A, B]](_.keyedLeft())
    )
  }
  
  implicit def RightKeyedFormat[A, B](implicit keyFormat: Format[B],
                                               valuesFormat: Format[Set[A]])): Format[JoinMap[A, B]] =
    Format(
      LeftKeyedFormat[A, B].map(_.flip()),
      LeftKeyedFormat[A, B].contramap(_.flip())
    )
}
