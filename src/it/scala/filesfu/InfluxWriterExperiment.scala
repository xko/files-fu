package filesfu

import akka.actor.ActorSystem
import akka.stream.alpakka.influxdb.InfluxDbWriteMessage
import akka.stream.alpakka.influxdb.scaladsl.InfluxDbSink
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import org.influxdb.dto.Point
import org.influxdb.{InfluxDB, InfluxDBFactory}
import org.scalatest.funspec.AnyFunSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}

class InfluxWriterExperiment  extends AnyFunSpec  {
  describe("alpakka"){
    it("writes metrics"){
      implicit val aX: ActorSystem = ActorSystem("InfluxWriterExperiment")
      implicit val eX: ExecutionContextExecutor = aX.dispatcher

      val conf = ConfigFactory.load().getConfig("influxdb")
      implicit val influxDB: InfluxDB = InfluxDBFactory.connect(
        "%s://%s:%s".format(conf.getString("protocol"), conf.getString("hostname"),conf.getString("port")) ,
        conf.getString("authentication.user"), conf.getString("authentication.password")
      ).setDatabase(conf.getString("database"))

      def up(v: String, c: Double, f: Int) = InfluxDbWriteMessage(
        Point.measurement("cpu").tag("version", v).addField("ourCPU", c).addField("filesShared",f).build()
        )

      val source = Source( List(
        up("604"     ,0.8504853713  ,10086       ),
        up("604"     ,0.491015952   ,26          ),
        up("604"     ,0.13278734    ,2           ),
        up("604"     ,0.5647926941  ,53          ),
        up("604"     ,0.643410821   ,156         ),
        up("604"     ,0.845952763   ,10096       ),
        up("604"     ,0.6489893475  ,186         ),
        up("604"     ,0.3262891318  ,7           ),
        up("604"     ,0.310091107   ,6           ),
        up("604"     ,0.4132545034  ,13          ),
        up("604"     ,0.7349821757  ,593         ),
        up("604"     ,0.2446534671  ,4           ),
        up("604"     ,0.3445465504  ,8           ),
        up("604"     ,0.7570990282  ,1006        ),
        up("604"     ,0.7314461453  ,647         ),
        up("604"     ,0.8328412272  ,5572        ),
        up("604"     ,0.8341247072  ,5598        ),
        up("604"     ,0.8463821041  ,10428       ),
        up("604"     ,0.6219422934  ,127         ),
        up("604"     ,0.3525811556  ,8           ),
        up("604"     ,0.6246614682  ,124         ),
        up("604"     ,0.73211873    ,600         ),
        up("604"     ,0.765961083   ,1085        ),
        up("604"     ,0.2810486417  ,5           ),
        up("604"     ,0.7985314623  ,2225        ),
        up("604"     ,0.2827258167  ,5           ),
        up("604"     ,0.6285763257  ,127         ),
        up("604"     ,0.5598572012  ,52          ),
        up("604"     ,0.2026656941  ,3           ),
        up("604"     ,0.7332797685  ,607         ),
        up("604"     ,0.6243319618  ,129         ),
        up("604"     ,0.2835622376  ,5           ),
        up("604"     ,0.5508326814  ,53          ),
        up("604"     ,0.5582527743  ,56          ),
        up("604"     ,0.7326777459  ,649         ),
        up("604"     ,0.1347811471  ,2           ),
        up("604"     ,0.2798874499  ,5           ),
        up("421"     ,0.3972601877  ,156         ),
        up("421"     ,0.3922907061  ,129         ),
        up("421"     ,0.4713727506  ,223         ),
        up("421"     ,0.1050754316  ,3           ),
        up("421"     ,0.4344806625  ,155         ),
        up("421"     ,0.5545101032  ,2504        ),
        up("421"     ,0.6101414833  ,5603        ),
        up("421"     ,0.6064389654  ,2446        ),
        up("421"     ,0.1736366695  ,5           ),
        up("421"     ,0.1429002792  ,4           ),
        up("421"     ,0.3803266381  ,55          ),
        up("421"     ,0.3981493548  ,122         ),
        up("421"     ,0.5195348862  ,574         ),
        up("421"     ,0.3768929883  ,56          ),
        up("421"     ,0.5353196565  ,1004        ),
        up("421"     ,0.5485478693  ,2228        ),
        up("421"     ,0.6285612739  ,5626        ),
        up("421"     ,0.3701594747  ,52          ),
        up("421"     ,0.1668242972  ,6           ),
        up("421"     ,0.07455972908 ,2           ),
        up("421"     ,0.2843951385  ,27          ),
        up("421"     ,0.2053753481  ,8           ),
        up("421"     ,0.2261361953  ,12          ),
        up("421"     ,0.6329580977  ,5558        ),
        up("421"     ,0.5152194654  ,601         ),
        up("421"     ,0.4704122311  ,208         ),
        up("421"     ,0.4833671926  ,570         ),
        up("421"     ,0.1065096377  ,3           ),
        up("421"     ,0.4535825325  ,171         ),
        up("421"     ,0.1962065706  ,7           ),
        up("421"     ,0.6355570227  ,10231       ),
        up("421"     ,0.4081109358  ,155         ),
        up("421"     ,0.5850367055  ,2640        ),
        up("421"     ,0.5137724313  ,570         ),
        up("421"     ,0.150253404   ,4           ),
        up("421"     ,0.5148424223  ,574         ),
        up("421"     ,0.2990333561  ,28          ),
        up("421"     ,0.4039437553  ,161         ),
        up("421"     ,0.3906791955  ,123         ),
        up("421"     ,0.4917191007  ,644         )
      )).map(m => List(m))

      val done = source.throttle(1, 1.second).runWith(InfluxDbSink.create())
      done.onComplete(_ => aX.terminate())
      Await.result(done, Duration.Inf)
    }
  }

}
