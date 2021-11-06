package filesfu.collector

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import com.typesafe.config.ConfigFactory
import filesfu.collector.protocol.Messages._
import filesfu.collector.protocol.JSONMarshalling._
import filesfu.collector.protocol.InfluxMarshalling._
import org.influxdb.{InfluxDB, InfluxDBFactory}

import scala.util.{Failure, Success}


object Server {
  implicit val system: ActorSystem = ActorSystem("FilesFU")
  import system.dispatcher

  val conf = ConfigFactory.load().getConfig("influxdb")

  implicit val influxDB: InfluxDB = InfluxDBFactory.connect(
    "%s://%s:%s".format(conf.getString("protocol"), conf.getString("hostname"),conf.getString("port")) ,
    conf.getString("authentication.user"), conf.getString("authentication.password")
    ).setDatabase(conf.getString("database"))


  val routes = pathPrefix("streams"){
    concat(
      path("sessions") {
        post {
          Routes.streaming(Influx.writer[Session]())
        }
      }
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
