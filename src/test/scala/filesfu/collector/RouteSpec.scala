package filesfu.collector

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RouteSpec  extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  "streaming route POC" should {
     "count sessions" in {
       val data = HttpEntity(
         ContentTypes.`application/json`,
         """{"id":"1234","version":"604","starting": true,"timestamp":0}
           |{"id":"1ee234","version":"420","starting": true,"timestamp":0}
           |{"id":"ahhh","version":"ohh","starting": true,"timestamp":0}
           |{"id":"OMG","version":"LOL","starting": true,"timestamp":0}
           |{"id":"ahhh","version":"ohhhh","starting": false,"timestamp":0}
           |{"id":"arghhh","version":"grr","starting": true,"timestamp":0}
           |{"id":"OMG","version":"LOL","starting": false,"timestamp":0}
           |{"id":"1ee234","version":"420","starting": false,"timestamp":0}
           |""".stripMargin)
       Post("/streams/sessions",entity = data) ~> Server.routes ~> check {
         status shouldBe StatusCodes.OK
         responseAs[String] shouldBe """{"msg":"Total messages received: 8"}"""
       }

    }
  }

}
