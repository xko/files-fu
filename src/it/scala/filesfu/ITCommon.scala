package filesfu

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import filesfu.collector.Server
import filesfu.collector.protocol.Messages.SessionState
import org.scalacheck.{Gen, Shrink}
import org.scalacheck.Gen.{choose, frequency, listOfN}
import org.scalatest.Suite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.Duration

trait ITCommon extends ScalatestRouteTest with Matchers { this: Suite =>
  type CPU = Double
  type EpochMs = Long

  val normal = frequency(90 -> choose(.05, .15), 30 -> choose(.15, .25),
                         10 -> choose(.25, .35), 3 -> choose(.40, .50))
  val high = frequency(5 -> choose(.05, .15), 20 -> choose(.15, .25),
                       20 -> choose(.25, .35), 30 -> choose(.40, .50),
                       10 -> choose(.50, .60))

  var extreme = choose(.85, .95)
  val normalWithSurges = frequency(80 -> normal, 20 -> extreme)
  val highWithSurges = frequency(80 -> high, 20 -> extreme)

  def higher(gen: Gen[CPU]) = gen.map(cpu => cpu * .70 + .30)


  val users = Gen.oneOf("Harry", "Larry", "Sally", "Molly")

  val IntervalMs = 500

  case class Phase(startAt: EpochMs, cpus: Seq[(CPU, EpochMs)])

  def listOfExactlyN[T](n: Int, gen: Gen[T]) = listOfN(n * 2, gen).map(_.take(n)).suchThat(_.size == n)

  def phaseGen(startAt: EpochMs, rules: (SessionState.Value, Gen[CPU], Duration)*): Gen[Map[SessionState.Value, Phase]] = rules.toList match {
    case Nil => Gen.const(Map.empty)
    case (state, cpus, duration) :: tail =>
      val times = startAt to startAt + duration.toMillis by IntervalMs
      for {thiz <- listOfExactlyN(times.size, cpus).map(_.zip(times)).map(state -> Phase(startAt, _))
           others <- phaseGen(startAt + duration.toMillis + 1, tail: _*)
           } yield others + thiz
  }
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

  implicit def noShrink[T]: Shrink[T] = Shrink.shrinkAny
  // https://gist.github.com/davidallsopp/f65d73fea8b5e5165fc3#gistcomment-2339650



}
