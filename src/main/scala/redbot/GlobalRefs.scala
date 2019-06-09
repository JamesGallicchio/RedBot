package redbot

import redbot.discord.User
import redbot.discord.Snowflake._

object GlobalRefs {
  val RedId: User.Id = 135553137699192832L.asId
  val Red: String = User.mention(RedId)
  val ServerInvite: String = "http://discord.gg/QVwRdk3"
}
