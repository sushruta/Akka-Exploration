package simplestreams

import akka.stream._
import akka.stream.scaladsl._

import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths

import scala.concurrent.ExecutionContext.Implicits.global

object SinkWithHead extends App {
  implicit val system = ActorSystem("akka-streams")
  implicit val materializer = ActorMaterializer()

  val nums = (1 to 10)
  val source = Source(nums)
  val sink = Sink.head[Int]
  val grph = Source.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val bcast = b.add(Broadcast[Int](2))
    val merge = b.add(Merge[Int](2))

    source ~> bcast.in
    bcast.out(0) ~> Flow[Int].map(_ * 3) ~> merge
    bcast.out(1) ~> Flow[Int].map(_ + 1) ~> merge

    SourceShape(merge.out)
  })

  grph.runWith(sink).foreach { c => println(s"the head value is $c") }
}
