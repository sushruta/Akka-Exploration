package DiningPhilosophers

import akka.actor.{ActorSystem, Props}

object DiningPhilosophers extends App {
  val system = ActorSystem("dining-philosophers")

  val philosopherNames = List[String]("Chanakya", "Pingala", "Brahaspati", "Patanjali", "Nachiketa", "Udayanacharya")
  val size = philosopherNames.length

  val chopsticks = for (i <- 1 to size) yield system.actorOf(Props(classOf[Chopstick], s"chopstick-$i"), s"chopstick-$i")
  
  val philosophers = for {
    (name, i) <- philosopherNames.zipWithIndex
  } yield system.actorOf(Props(classOf[Philosopher], name, chopsticks(i % size), chopsticks((i + 1) % size)), name)

  philosophers.foreach( _ ! Think )
}
