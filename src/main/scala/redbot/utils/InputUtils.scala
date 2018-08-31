package redbot.utils

object InputUtils {
  def levDistance(s1: String, s2: String): Int = {
    if (s1.length < s2.length) return levDistance(s2, s1)

    var firstRow = (0 to s2.length).toArray
    var secondRow = new Array[Int](firstRow.length)

    for (i <- s1.indices) {

      secondRow.update(0,i+1)

      for (j <- s2.indices) {
        val deletCost = firstRow(j+1)+1
        val insertCost = secondRow(j)+1
        val subCost = firstRow(j) + (if (s1.charAt(i) == s2.charAt(j)) 0 else 1)
        secondRow.update(j+1, Math.min(deletCost, Math.min(insertCost, subCost)))
      }

      val temp = firstRow
      firstRow = secondRow
      secondRow = temp
    }

    firstRow(firstRow.length-1)
  }

  def closestMatch(input: String, potentials: Seq[String]): (Int, Int) = {
    potentials.map(levDistance(input, _)).zipWithIndex.minBy{case (dist, _) => dist}
  }
}
