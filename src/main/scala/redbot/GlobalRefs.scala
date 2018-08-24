package redbot

import redbot.discord.User

object GlobalRefs {
  val RedId: User.Id = ???
  val Red: String = User.mention(RedId)
  val ServerInvite: String = ???
}
