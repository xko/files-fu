package filesfu

import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.testkit.TestDuration
import filesfu.collector.protocol.Messages._
import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.util.UUID
import scala.concurrent.duration._

class Simulation extends AnyWordSpec with Matchers with ScalaCheckPropertyChecks with ITCommon {

  implicit val timeout = RouteTestTimeout(10.seconds.dilated)




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
