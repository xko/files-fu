package filesfu.collector

import akka.actor.ActorSystem
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.server.Directives.{asSourceOf, complete, entity, path}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Keep, Sink}

object Routes {
  def streamingPOC(implicit system: ActorSystem): Route = {
    implicit val jsonStreamingSupport = EntityStreamingSupport.json()

    import system.dispatcher
    path("stream") {
      val coll = Sink.fold[Set[String], String](Set.empty[String])(_ + _)
      import Protocol._
      entity(asSourceOf[Protocol.Session]) { sessions =>
        val ss = sessions.map(_.id).toMat(coll)(Keep.right).run()
        complete {
          ss.map(s => Map("msg" -> s"""Unique sessions: ${s.size}"""))
        }
      }
    }
  }
}
