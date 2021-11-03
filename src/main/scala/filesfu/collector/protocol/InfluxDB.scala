package filesfu.collector.protocol

import akka.stream.scaladsl.Flow
import org.influxdb.dto.Point

import java.util.concurrent.TimeUnit


object InfluxDB {
  implicit val session = Flow[Session].map { s =>
    val b = Point.measurement("session").time(s.timestamp, TimeUnit.MILLISECONDS)
    s.version.foreach(b.tag("version", _))
    s.user.foreach(b.tag("user", _))
    b.build
  }

}
