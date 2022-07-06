import battle.battle._
import com.github.phisgr.gatling.grpc.Predef._
import com.github.phisgr.gatling.grpc.protocol.StaticGrpcProtocol
import com.github.phisgr.gatling.pb.{EpxrLens, value2ExprUpdatable}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.grpc.Status

import scala.concurrent.duration.{DurationInt, FiniteDuration}

class BasicSimulation extends Simulation {

  val playerId1: String = "4e0453ee-c7ba-11ec-9d64-0242ac120002"
  val playerId2: String = "541042b6-c7ba-11ec-9d64-0242ac120002"

  val delay: FiniteDuration = 2.seconds
  val shotDelay: FiniteDuration = 1.seconds

  val grpcConf: StaticGrpcProtocol = grpc(managedChannelBuilder(name = "localhost", port = 8080).usePlaintext())
  //.warmUpCall(BattleServiceGrpc.METHOD_START, Start.defaultInstance)

  val ships: Seq[Ship] = Seq(
    Ship(1, 1, Direction.Horisontal, 4),
    Ship(1, 3, Direction.Horisontal, 3),
    Ship(4, 3, Direction.Horisontal, 3),
    Ship(1, 5, Direction.Horisontal, 2),
    Ship(4, 5, Direction.Horisontal, 2),
    Ship(7, 5, Direction.Horisontal, 2),
    Ship(1, 7, Direction.Horisontal, 1),
    Ship(3, 7, Direction.Horisontal, 1),
    Ship(5, 7, Direction.Horisontal, 1),
    Ship(7, 7, Direction.Horisontal, 1)
  )

  val scn: ScenarioBuilder =
    scenario("Scenario Name")
      .exec(
        grpc("start")
          .rpc(BattleServiceGrpc.METHOD_START)
          .payload(Start(Some(Player(playerId1)), Some(Player(playerId2))))
          .extract(_.gameId.some)(_ saveAs "gameId")
          .check(statusCode is Status.Code.OK)
      )
      //      .exec { session: Session =>
      //        println(s"gameId: ${session("gameId").as[String]}")
      //        session
      //      }
      .pause(delay)
      .exec(
        grpc("setup")
          .rpc(BattleServiceGrpc.METHOD_SETUP)
          .payload(
            Setup("gameId", playerId1, ships)
              .updateExpr(_.gameId :~ $("gameId"))
          )
          .check(statusCode is Status.Code.OK)
      )
      .pause(delay)
      .exec(
        grpc("setup")
          .rpc(BattleServiceGrpc.METHOD_SETUP)
          .payload(
            Setup("gameId", playerId2, ships)
              .updateExpr(_.gameId :~ $("gameId"))
          )
          .check(statusCode is Status.Code.OK)
      )
      .pause(delay)
      .foreach(Seq(1,2,3,4,5,6,7,8,9,10), "y") {
        foreach(Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), "x") {
          pause(shotDelay)
          .exec(
            grpc("shot")
              .rpc(BattleServiceGrpc.METHOD_SHOT)
              .payload(
                Shot("gameId", playerId1)
                  .updateExpr(_.gameId :~ $("gameId"), _.x :~ $("x"), _.y :~ $("y"))
              )
              .check(statusCode is Status.Code.OK)
          )
          .pause(shotDelay)
          .exec(
            grpc("shot")
              .rpc(BattleServiceGrpc.METHOD_SHOT)
              .payload(
                Shot("gameId", playerId2, 1, 1)
                  .updateExpr(_.gameId :~ $("gameId"))
              )
              .check(statusCode is Status.Code.OK)
          )
        }
      }

  setUp(
    scn
    .inject(rampUsers(1000).during(10.seconds))
  )
    .protocols(grpcConf)

}