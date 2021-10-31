package filesfu.collector

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol



object Protocol extends SprayJsonSupport with DefaultJsonProtocol {
  case class Session(timestamp: Long, id: String, version: String, starting: Boolean)
  implicit val sessionFmt = jsonFormat4(Session.apply)

  case class CPU(timestamp: Long, session: String, percentage: Double )
  implicit val cpuFmt = jsonFormat3(CPU.apply)


}
