package filesfu.collector

import akka.actor.ActorSystem
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromByteStringUnmarshaller
import akka.stream.scaladsl.Flow
import Protocol._

import scala.concurrent.Future

object Routes {
  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  def streaming[In: FromByteStringUnmarshaller](via: Flow[In, _, _])(implicit system: ActorSystem): Route = {
    import system.dispatcher
    withoutSizeLimit {
      entity(asSourceOf[In]) { msgs =>
        val evTotal: Future[Int] = msgs.via(via).runFold(0) { (total, _) => total + 1 }
        complete {
          evTotal.map(total => Map("msg" -> s"""Total messages received: $total"""))
        }
      }
    }
  }
}
