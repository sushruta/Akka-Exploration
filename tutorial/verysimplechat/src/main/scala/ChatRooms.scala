package VerySimpleChat

import akka.actor.{ActorSystem, FSM}

/*
sealed trait ChatApplicationEvent
case class CreateRoomInChatApplication(roomId: Int) extends ChatApplicationEvent
case class NewUserInChatApplication(userName: String, roomId: Int) extends ChatApplicationEvent
case class JoinRoomInChatApplication(userId: Int, roomId: Int) extends ChatApplicationEvent
case object RoomNotFoundChatApplication extends ChatApplicationEvent

sealed trait ChatApplicationState
case object ChatApplicationInitialized extends ChatApplicationState
case object ChatApplicationRunning extends ChatApplicationState

case class ChatApplicationData(chatRooms: Map[Int, ChatRoom])

class ChatApplicationManager(appName: String) extends FSM[ChatApplicationState, ChatApplicationData] {
	startWith(ChatApplicationInitialized, ChatApplicationData(Map.empty[Int, ChatRoom]))

	when(ChatApplicationInitialized) {
		case Event(CreateRoomInChatApplication(roomId), ChatApplicationData(chatRooms: Map[Int, ChatRoom])) => {
			val chatRoom = Cha
		}
			goto(ChatApplicationRunning) using ChatApplicationData(chatRooms + (roomId -> ChatRoom(roomId)))

		case Event(JoinRoomInChatApplication(_, _), _) =>
			log.warning(s"invalid request for $appName in $stateName. Can't find a room in an app with no rooms!")
			stay

		case Event(e, s) =>
			log.warning(s"$appName received unhandled request $e in state $stateName/$s")
			stay
	}

	when(ChatApplicationRunning) {
		case Event(CreateRoomInChatApplication(roomId: Int), ChatApplicationData(chatRooms: Map[Int, ChatRoom])) =>
			goto(ChatApplicationRunning) using ChatApplicationData(chatRooms + (roomId -> ChatRoom(roomId)))

		case Event(JoinRoomInChatApplication(userId: Int, roomId: Int), ChatApplicationData(chatRooms: Map[Int, ChatRoom])) =>
			chatRooms get roomId match {
				case None => stay replying RoomNotFoundChatApplication
				// case Some(chatRoom: ChatRoom) => TODO add this code
			}

		case Event(e, s) =>
			log.warning(s"$appName received unhandled request $e in state $stateName/$s")
			stay
	}

	initialize()
}
*/

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
