package chatapp

import akka.actor.ActorSystem

object Channels {
  var channels: Map[Int, Channel] = Map.empty[Int, Channel]

  def findOrCreate(channelId: Int)(implicit actorSystem: ActorSystem): Channel = {
    channels.getOrElse(channelId, createNewChannel(channelId))
  }

  def createNewChannel(channelId: Int)(implicit actorSystem: ActorSystem): Channel = {
    val channel = Channel(channelId)
    channels += (channelId -> channel)
    channel
  }
}