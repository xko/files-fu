package filesfu.collector

import akka.actor.ActorSystem
import akka.http.javadsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.server.Directives.{asSourceOf, complete, entity, path}
import akka.http.scaladsl.server.Route

import scala.concurrent.Future

object Routes {
  def streamingPOC(implicit system: ActorSystem): Route = {
    implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

    import system.dispatcher
    path("stream") {
      import Protocol._
      entity(asSourceOf[Protocol.Session]) { sessions =>
        // alternative syntax:
        // entity(as[Source[Measurement, NotUsed]]) { measurements =>
        val runningSessions: Future[Int] =
          sessions.runFold(0) { (ss, s) => if (s.started) ss + 1 else ss - 1 }
        complete {
          runningSessions.map(n => Map("msg" -> s"""Sessions still running: $n"""))
        }
      }
    }
  }
}
