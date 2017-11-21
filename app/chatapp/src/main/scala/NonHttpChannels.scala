package chatapp

import akka.actor.ActorSystem

object NonHttpChannels {
  var channels: Map[Int, NonHttpChannel] = Map.empty[Int, NonHttpChannel]

  def findOrCreate(channelId: Int)(implicit actorSystem: ActorSystem): NonHttpChannel = {
    channels.getOrElse(channelId, createNewChannel(channelId))
  }

  def createNewChannel(channelId: Int)(implicit actorSystem: ActorSystem): NonHttpChannel = {
    val channel = NonHttpChannel(channelId)
    channels += (channelId -> channel)
    channel
  }
}
