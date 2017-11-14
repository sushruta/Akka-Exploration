package chatapp

sealed trait SomeoneWhoCanChat

case class User(username: String) extends SomeoneWhoCanChat