package simplestreams

import akka.stream._
import akka.stream.scaladsl._

import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import scala.concurrent._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

object PassSink extends App {
  implicit val system = ActorSystem("akka-streams")
  implicit val materializer = ActorMaterializer()

  val topHeadSink = Sink.head[Int]
  val bottomHeadSink = Sink.head[Int]
  val sharedDoubler = Flow[Int].map(_ * 2)

  val grph = RunnableGraph.fromGraph(GraphDSL.create(topHeadSink, bottomHeadSink)((_, _)) {
    implicit bldr =>
      (topHS, bottomHS) =>
        import GraphDSL.Implicits._

        val bcast = bldr.add(Broadcast[Int](2))
        Source(1 to 10) ~> bcast.in

        bcast.out(0) ~> sharedDoubler ~> topHS.in
        bcast.out(1) ~> sharedDoubler ~> bottomHS.in
        ClosedShape
  })

  val futureResults = grph.run()
  futureResults match {
    case (f1: Future[Int], f2: Future[Int]) =>
      f1.foreach { c => println(s"I have :: $c") }
      f2.foreach { c => println(s"I also have :: $c") }
    case _ => println("this wasn't expected")
  }
}
