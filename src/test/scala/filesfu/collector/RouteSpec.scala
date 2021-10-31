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
         """{"sid":"1234","version":"604","started": true}
           |{"sid":"1ee234","version":"420","started": true}
           |{"sid":"ahhh","version":"ohh","started": true}
           |{"sid":"OMG","version":"LOL","started": false}
           |{"sid":"arghhh","version":"grr","started": true}
           |""".stripMargin)
       Post("/stream",entity = data) ~> Routes.streamingPOC ~> check {
         status shouldBe StatusCodes.OK
         responseAs[String] shouldBe """{"msg":"Sessions still running: 3"}"""
       }

    }
  }

}
