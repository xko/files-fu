package filesfu.collector

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalacheck.Gen.choose
import org.scalacheck.{Gen, Shrink}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.testkit.TestDuration
import scala.concurrent.duration._


class InfluxIntegration extends AnyWordSpec with Matchers with ScalatestRouteTest with ScalaCheckPropertyChecks {
  implicit def noShrink[T]: Shrink[T] = Shrink.shrinkAny
  // https://gist.github.com/davidallsopp/f65d73fea8b5e5165fc3#gistcomment-2339650

  implicit val timeout = RouteTestTimeout(10.seconds.dilated)

  "streams/sessions" can {
    val times = Iterator.iterate(System.currentTimeMillis())(_ - 1000)

    def postLines(lines: String*) =
      Post("/streams/sessions").withEntity(
        HttpEntity(ContentTypes.`application/json`, lines.mkString("\n"))
      ) ~> Server.routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe s"{\"msg\":\"Total messages received: ${lines.size}\"}"
      }

    "write some sessions with cpu peaks " in {
      val cpustats = Gen.frequency(80 -> choose(.05, .25),
                                   40 -> choose(.25, .30),
                                   20 -> choose(.30, .40),
                                   10 -> choose(.40, .50),
                                   5  -> choose(.85, .88),
                                   10 -> choose(.88, .95))
      val users = Seq("Harry", "Larry", "Sally", "Molly")
      val sessions = users.map(u => s"${u}s-Session")
      val version = "604"
      sessions.foreach { s =>
        postLines(s"{\"sessionID\":\"$s\", \"version\": \"$version\", \"state\":\"start\", \"timestamp\": ${times.next()} }")
      }

      forAll(Gen.listOfN(15,cpustats), Gen.oneOf(sessions), MinSuccessful(100)) { (cpus, session) =>
        val lines = cpus.map(cpu => s"{\"timestamp\":${times.next()},\"sessionID\":\"$session\", \"cpu\":$cpu }")
        postLines(lines:_*)
      }
    }
  }

}
