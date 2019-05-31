package redbot.utils

import play.api.libs.json.Format

import scala.collection.Map
import scala.collection.mutable
import scala.collection.immutable.Set

class JoinMap[L, R] private (private val backingL: mutable.Map[L, Set[R]],
                             private val backingR: mutable.Map[R, Set[L]]) {
  def getL(key: L): Option[Set[R]] = backingL.get(key)
  def getR(key: R): Option[Set[L]] = backingR.get(key)
  
  def join(left: L, right: R): Unit = {
    backingL(left)  = backingL.getOrElse(left,  Set.empty) + right
    backingR(right) = backingR.getOrElse(right, Set.empty) + left
  }
  
  def unjoin(left: L, right: R): Boolean =
    (for {
      rSet <- getL(left)
      if rSet contains right
      lSet <- getR(right)
      if lSet contains left
    } yield {
      backingL(left) = rSet - right
      backingR(right) = lSet - left
    }).isDefined
  
  def isJoined(left: L, right: R): Boolean =
    getL(left).exists(_ contains right)
  
  def flip(): JoinMap[R, L] = new JoinMap(backingR.clone(), backingL.clone())
  
  override def clone(): JoinMap[L, R] = new JoinMap(backingL.clone(), backingR.clone())
  
  def keyedLeft: Map[L, Set[R]] = backingL
  def keyedRight: Map[R, Set[L]] = backingR

  def keysL: Iterable[L] = backingL.keys
  def keysR: Iterable[R] = backingR.keys
}

object JoinMap {
  def empty[L, R]: JoinMap[L, R] = new JoinMap(mutable.Map.empty, mutable.Map.empty)
  
  def apply[L, R](map: Map[L, Set[R]]): JoinMap[L, R] = {
    val j = JoinMap.empty[L, R]
    for {
      (l, rSet) <- map
      r <- rSet
    } j.join(l, r)
    j
  }
  
  implicit def LeftKeyedFormat[A, B](implicit keyFormat: Format[A],
                                              valuesFormat: Format[B]): Format[JoinMap[A, B]] = {
    import play.api.libs.json.Reads
    import play.api.libs.json.Writes
    
    Format(
      Reads.ArrayReads[(A, Set[B])].map { arr => JoinMap(arr.toMap) },
      (o: JoinMap[A, B]) => Writes.arrayWrites[(A, Set[B])].writes(o.keyedLeft.toArray)
    )
  }

  implicit def RightKeyedFormat[A, B](implicit keyFormat: Format[B],
                                     valuesFormat: Format[A]): Format[JoinMap[A, B]] = {
    import play.api.libs.json.Reads
    import play.api.libs.json.Writes

    Format(
      Reads.ArrayReads[(B, Set[A])].map { arr => JoinMap(arr.toMap).flip() },
      (o: JoinMap[A, B]) => Writes.arrayWrites[(B, Set[A])].writes(o.keyedRight.toArray)
    )
  }
}
