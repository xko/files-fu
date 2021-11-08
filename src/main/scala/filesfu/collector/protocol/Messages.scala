package filesfu.collector.protocol

/**
 * Formal definitions of the protocol messages - these translate directly to the JSON structure,
 * we expect from client. [[InfluxMarshalling]] defines the InfluxDB representation.
 */
object Messages {

  /** A phase of the session lifecycle */
  implicit object SessionState extends Enumeration {
    /** Session is starting. Sent ASAP when client app starts, [[Session.version]] normally comes here  */
    val start = Value
    /** Authentication and login. At this point we should know [[Session.userID]] */
    val login = Value
    /** Syncing/hashing/indexing the files on local disk. Should complete before actual sharing starts */
    val sync = Value
    /** Normal operation - sending and receiving files */
    val running = Value
    /** Shutdown is requested */
    val shutdown = Value
    /** Client app is closed. Should be the last message in the session */
    val over = Value
  }

  /**
   * Event and/or CPU measurement from particular client session. Optional fields can come in any combination -
   * current reporting impl treats them independently and matches by the timestamp sequence. Some data
   * (version, user) is supposed to come with particular state, but this is not strictly required.
   *
   * @param timestamp (epoch milliseconds) is set at the client, unrelated to actual message delivery
   * @param sessionID GUID unique per each app lifecycle on each client device
   * @param state the [[SessionState]]
   * @param version version of the client app
   * @param userID the user, as soon as it's known
   * @param cpu (double within 0..1) the percentage of CPU used by client app
   */
  case class Session(timestamp: Long, sessionID: String,
                     state: Option[SessionState.Value], version: Option[String], userID: Option[String],
                     cpu: Option[Double])


  /**
   * (Currently not used) The state of particular file.
   */
  implicit object FileState extends Enumeration {
    val share, hash, seed, send, request, receive, remove, gone = Value
  }

  /**
   * (Currently not used) Tracks single file in the sharing session.
   * The idea is to correlate CPU with number of files in particular state, their properties etc.
   */
  case class File(timestamp: Long, fileID: String, sessionID: String,
                  state: Option[FileState.Value], haveBytes: Option[Long], totalBytes: Option[Long])

}
