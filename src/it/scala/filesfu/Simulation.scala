package filesfu

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import filesfu.collector.Server
import filesfu.collector.protocol.Messages._
import org.scalacheck.{Gen, Shrink}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.util.UUID
import scala.concurrent.duration._

class Simulation extends AnyWordSpec with Matchers with ScalatestRouteTest with ScalaCheckPropertyChecks
  with SessionGenerators {

  implicit def noShrink[T]: Shrink[T] = Shrink.shrinkAny
  // https://gist.github.com/davidallsopp/f65d73fea8b5e5165fc3#gistcomment-2339650

  implicit val timeout = RouteTestTimeout(10.seconds.dilated)

  def postLines(path: String, lines: String*) = {
    println(lines.mkString("\n"))
    Post(path).withEntity(HttpEntity(ContentTypes.`application/json`, lines.mkString("\n"))) ~>
      Server.routes ~> check {
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe s"{\"msg\":\"Total messages received: ${lines.size}\"}"
    }
  }

  def postCPU(path: String, sid: String, cpus: (Double, EpochMs)*) = {
      val lines = cpus.map { case (cpu, time) =>
        s"{\"timestamp\":$time,\"sessionID\":\"$sid\", \"cpu\":$cpu }"
      }
      postLines(path, lines: _*)
    }


  "client" can  {
    val Path = "/streams/sessions"
    import SessionState._

    def postSession(user: String, version: String, sid: UUID, phase: Map[SessionState.Value, Phase]) = {
      postLines(Path, s"{\"sessionID\":\"$sid\", \"version\": \"$version\", \"state\":\"start\", \"timestamp\": ${phase(start).startAt} }")
      postCPU(Path, sid.toString, phase(start).cpus: _*)
      postLines(Path, s"{\"sessionID\":\"$sid\", \"userID\": \"$user\", \"state\":\"login\", \"timestamp\": ${phase(login).startAt} }")
      postCPU(Path, sid.toString, phase(login).cpus: _*)
      postLines(Path, s"{\"sessionID\":\"$sid\", \"state\":\"running\", \"timestamp\": ${phase(running).startAt} }")
      postCPU(Path, sid.toString, phase(running).cpus: _*)
      postLines(Path, s"{\"sessionID\":\"$sid\", \"state\":\"shutdown\", \"timestamp\": ${phase(shutdown).startAt} }")
      postCPU(Path, sid.toString, phase(shutdown).cpus: _*)
      postLines(Path, s"{\"sessionID\":\"$sid\", \"state\":\"over\", \"timestamp\": ${phase(over).startAt} }")
    }

    "post sessions with high CPU at login"  in {
      val versions = Gen.oneOf(Seq("700"))
      val phases = phaseGen(System.currentTimeMillis() - 3600000,
                            (start, normal, 2.seconds),
                            (login, highWithSurges, 4.seconds),
                            (running, higher(normal), 10.seconds),
                            (shutdown, normal, 5.seconds),
                            (over, normal, 0.seconds)
                            )
      val sessionCount = minSuccessful(5)
      forAll(users, versions, Gen.uuid, phases, sessionCount)(postSession)
    }

    "post normal sessions" in {
      val versions = Gen.oneOf(Seq("604","605"))
      val phases = phaseGen(System.currentTimeMillis() - 3600000,
                            (start, normal, 2.seconds),
                            (login, normal, 4.seconds),
                            (running, normal, 20.seconds),
                            (shutdown, normal, 5.seconds),
                            (over, normal, 0.seconds)
                            )
      val sessionCount = minSuccessful(5)
      forAll(users, versions, Gen.uuid, phases, sessionCount)(postSession)
    }


  }

}
