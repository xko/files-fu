package filesfu.collector

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import filesfu.collector.protocol._
import filesfu.collector.protocol.JSON._

import scala.util.{Failure, Success}


object Server {
  implicit val system: ActorSystem = ActorSystem("FilesFU")
  import system.dispatcher

  val routes = pathPrefix("streams"){
    concat(
      path("sessions")(Routes.streaming(Flow[Session]))
    )
  }

  def start(routes: Route)(implicit system: ActorSystem): Unit = {
    val futureBinding = Http().newServerAt("0.0.0.0", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    start(routes)
  }

}
