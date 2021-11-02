package filesfu.collector

import akka.NotUsed
import akka.stream._
import akka.stream.alpakka.influxdb.{InfluxDbWriteMessage, InfluxDbWriteResult}
import akka.stream.alpakka.influxdb.scaladsl.InfluxDbFlow
import akka.stream.scaladsl._
import org.influxdb.InfluxDB
import org.influxdb.dto.Point

object Influx  {

  type Msg = InfluxDbWriteMessage[Point, NotUsed]

  def pointSink(perGroup: Int = 5, parGroups:Int = 20)(implicit db:InfluxDB) =
    pointFlow(perGroup,parGroups).toMat(Sink.ignore)(Keep.right)

  def pointFlow(perGroup: Int = 5, parGroups:Int = 20)(implicit db:InfluxDB): Flow[Point, Seq[InfluxDbWriteResult[Point, NotUsed]], NotUsed] =
    Flow[Point] map InfluxDbWriteMessage.apply via groupedFlow(InfluxDbFlow.create(), perGroup, parGroups)

  def groupedFlow[In,Out](worker: Flow[Seq[In],Out, NotUsed], perGroup: Int = 5, parGroups:Int = 20): Flow[In, Out, NotUsed] = {
    import GraphDSL.Implicits._
    Flow[In].grouped(perGroup).via( GraphDSL.create() { implicit b =>
      val balancer = b.add(Balance[Seq[In]](parGroups))
      val merge = b.add(Merge[Out](parGroups))
      for (_ <- 1 to parGroups) { balancer ~> worker.async ~> merge }
      FlowShape(balancer.in, merge.out)
    })
  }



}
