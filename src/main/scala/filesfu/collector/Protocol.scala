package filesfu.collector

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


object Protocol extends SprayJsonSupport with DefaultJsonProtocol {

  case class Session(sid: String, version: String, started: Boolean)
  implicit val sessionFmt: RootJsonFormat[Session] = jsonFormat3(Session.apply)

}
