package filesfu.collector.protocol

import akka.stream.scaladsl.Flow
import org.influxdb.dto.Point

import java.util.concurrent.TimeUnit


object InfluxPoints {
  implicit val sessionPoints = Flow[Session].map { s =>
    val b = Point.measurement("session").time(s.timestamp, TimeUnit.MILLISECONDS)
                 .tag("sessionID",s.sessionID)
    s.version.foreach(b.tag("version", _))
    s.state.foreach(v => b.tag( "sessionState",v.toString))
    s.userID.foreach(b.tag("userID", _))
    s.cpu.foreach(b.addField("cpu",_))
    b.addField("_",true)
    b.build
  }

}
