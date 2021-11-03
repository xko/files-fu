package filesfu.collector

package object protocol {
  implicit object SessionState extends Enumeration {
    val start, login, sync, run, shutdown, over = Value
  }

  case class Session(timestamp: Long, id: String,
                     state: Option[SessionState.Value], version: Option[String], user: Option[String])


  implicit object FileState extends Enumeration {
    val share, hash, seed, send, request, receive, remove, gone = Value
  }

  case class File(timestamp: Long, id: String, session: String,
                  state: Option[FileState.Value], haveBytes: Option[Long], totalBytes: Option[Long])


  case class CPU(timestamp: Long, session: String, cpu: Double)

}
