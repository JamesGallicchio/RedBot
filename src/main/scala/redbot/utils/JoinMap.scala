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
}
