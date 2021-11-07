package filesfu

import akka.http.scaladsl.model.StatusCodes
import akka.stream.alpakka.influxdb.scaladsl.InfluxDbSource
import akka.stream.scaladsl.Sink
import org.influxdb.dto.Query
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._



class Acceptance extends AnyWordSpec with ITCommon with ScalaFutures {

  val sid = "theSid"
  val version = "theVersion"
  val time = System.currentTimeMillis() - 3600000*24*10 //long ago to avoid interference with reports


  "InfluxDB" should {
    "contain what we posted" in {
      import filesfu.collector.Server.influxDB
      justPost("/streams/sessions", s"{\"sessionID\":\"$sid\", \"version\": \"$version\", \"state\":\"start\", \"timestamp\": $time }") ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe s"{\"msg\":\"Total messages received: 1\"}"
        val query = new Query(s"SELECT * FROM session WHERE time < ${time+1}ms ", "FilesFU")
        val tbResult = InfluxDbSource(influxDB, query).runWith(Sink.seq)
        whenReady(tbResult, timeout(5.seconds)){ r =>
          r.head.getResults.get(0).getSeries.get(0).getValues.asScala should contain (
            new java.util.ArrayList(List(Instant.ofEpochMilli(time).toString,true,sid,"start",version).asJava)
          )
        }
      }


    }
  }

}
