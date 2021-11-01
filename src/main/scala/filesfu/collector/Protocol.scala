package filesfu.collector

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._



object Protocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit def enumFormat[T <: Enumeration](implicit enu: T): RootJsonFormat[T#Value] =
    new RootJsonFormat[T#Value] {
      def write(obj: T#Value): JsValue = JsString(obj.toString)
      def read(json: JsValue): T#Value = {
        json match {
          case JsString(txt) => enu.withName(txt)
          case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
        }
      }
    }

  implicit object SessionState extends Enumeration {
    val start, login, sync, run, shutdown, over= Value
  }

  case class Session(timestamp: Long, id: String,
                     state: Option[SessionState.Value], version: Option[String], user: Option[String])

  implicit val sessionFmt = jsonFormat5(Session.apply)

  implicit object FileState extends Enumeration {
    val share, hash, seed, send, request, receive, remove, gone = Value
  }

  case class File(timestamp: Long, id: String, session: String,
                  state: Option[FileState.Value], haveBytes: Option[Long], totalBytes: Option[Long] )


  case class CPU(timestamp: Long, session: String, cpu: Double )
  implicit val cpuFmt = jsonFormat3(CPU.apply)


}
