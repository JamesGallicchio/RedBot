package redbot.utils

import scala.util.Try
import java.net.URL

object InputUtils {
  /**
    * Levenshtein distance between two strings
    * @param s1 first string to compare
    * @param s2 second string to compare
    * @return the Levenshtein distance
    */
  def distance(s1: String, s2: String, ignoreCaps: Boolean = false): Int =
    if (s1.length > s2.length)
      levDistance(s1, s2, ignoreCaps)
    else
      levDistance(s2, s1, ignoreCaps)

  def closeness(s1: String, s2: String, ignoreCaps: Boolean = false): Int =
    Math.max(s1.length, s2.length) - distance(s1, s2, ignoreCaps)

  private def levDistance(s1: String, s2: String, ignoreCaps: Boolean = false): Int = {
    var firstRow = (0 to s2.length).toArray
    var secondRow = new Array[Int](firstRow.length)

    for (i <- s1.indices) {

      secondRow(0) = i+1

      for (j <- s2.indices) {
        val deletCost = firstRow(j+1)+1
        val insertCost = secondRow(j)+1
        val subCost = firstRow(j) +
          (if (s1.charAt(i) == s2.charAt(j) ||
            (ignoreCaps && s1.charAt(i).toLower == s2.charAt(j).toLower)) 0 else 1)

        secondRow(j+1) = Math.min(deletCost, Math.min(insertCost, subCost))
      }

      val temp = firstRow
      firstRow = secondRow
      secondRow = temp
    }

    firstRow(firstRow.length-1)
  }
  
  def verifyURL(url: String): Try[URL] = Try {
    val u = new URL(url)
    val conn = u.openConnection()
    conn.connect()
    
    val is = conn.getInputStream
    val finalUrl = conn.getURL
    is.close()
    
    finalUrl
  }
}
