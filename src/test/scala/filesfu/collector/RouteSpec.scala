package filesfu.collector

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.{Flow, Keep, Sink}
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestProbe
import filesfu.collector.protocol.{Session, SessionState}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import protocol.JSON._

class RouteSpec  extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  "streaming route" should {
     "receive sessions " in {
       val data = HttpEntity(
         ContentTypes.`application/json`,
         """{"id":"12344","version":"604", "timestamp": 1635967999}
           |{"id":"12342","version":"420", "timestamp":0, "state": "start" }
           |{"id":"ahhh","version":"ohh", "timestamp": 1635967999, "state": "shutdown"}
           |{"id":"OMG","version":"LOL","user":"Sally","timestamp":1635966999}
           |{"id":"ahhh", "state": "sync","version":"ohhhh", "garbage": "trash", "timestamp":640, "user": "Harry"}
           |
           |{"id":"arghhh","timestamp":0}
           |""".stripMargin)
       val probe = TestProbe()
       val f = Flow[Session].alsoTo(Sink.actorRef(probe.testActor,"done",_=>"failed"))
       Post().withEntity(data) ~> Routes.streaming(f) ~> check {
         probe.expectMsgAllOf(Session(1635967999,"12344",None,Some("604"), None),
                              Session(0, "12342", Some(SessionState.start), Some("420"), None),
                              Session(1635967999, "ahhh", Some(SessionState.shutdown), Some("ohh"), None),
                              Session(1635966999, "OMG", None, Some("LOL"), Some("Sally")),
                              Session(640, "ahhh", Some(SessionState.sync), Some("ohhhh"), Some("Harry")),
                              Session(0, "arghhh", None, None, None)
                              )
         status shouldBe StatusCodes.OK
         responseAs[String] shouldBe """{"msg":"Total messages received: 6"}"""

       }

    }
  }

}
