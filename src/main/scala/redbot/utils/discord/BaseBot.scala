package redbot.utils.discord

import discord4j.core.DiscordClient
import discord4j.core.event.domain.Event
import reactor.core.scala.publisher.Flux

trait BaseBot {
  def token: String
  def name: String
  def helpText: String

  def consumeEvents(evFlux: Flux[Event]): Unit
}
