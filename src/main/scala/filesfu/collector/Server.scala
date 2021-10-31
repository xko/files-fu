package filesfu.collector

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}


object Server {

  def start(routes: Route)(implicit system: ActorSystem): Unit = {
    implicit val eX: ExecutionContextExecutor = system.dispatcher

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
    implicit val system: ActorSystem = ActorSystem("InfluxWriterExperiment")
    start(Routes.streamingPOC)

  }

}
