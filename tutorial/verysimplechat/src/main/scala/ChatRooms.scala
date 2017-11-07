package VerySimpleChat

import akka.actor.{ActorSystem, FSM}

object ChatRooms {
    var chatRooms: Map[Int, ChatRoom] = Map.empty[Int, ChatRoom]

    def findOrCreate(number: Int)(implicit actorSystem: ActorSystem): ChatRoom = {
        chatRooms.getOrElse(number, createNewChatRoom(number))
    }

    def createNewChatRoom(number: Int)(implicit actorSystem: ActorSystem): ChatRoom = {
        val chatRoom = ChatRoom(number)
        chatRooms += number -> chatRoom
        chatRoom
    }
}
