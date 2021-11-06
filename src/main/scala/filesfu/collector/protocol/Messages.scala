package filesfu.collector.protocol

object Messages {
  implicit object SessionState extends Enumeration {
    val start, login, sync, running, shutdown, over = Value
  }

  case class Session(timestamp: Long, sessionID: String,
                     state: Option[SessionState.Value], version: Option[String], userID: Option[String],
                     cpu: Option[Double])


  implicit object FileState extends Enumeration {
    val share, hash, seed, send, request, receive, remove, gone = Value
  }

  case class File(timestamp: Long, fileID: String, session: String,
                  state: Option[FileState.Value], haveBytes: Option[Long], totalBytes: Option[Long])

}
