package services.session

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import javax.inject.Inject
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

class ClusterSystem @Inject() (configuration: Configuration, applicationLifecycle: ApplicationLifecycle) {
  private val systems = startup(Seq("2551", "2552"))

  def startup(ports: Seq[String]): Seq[ActorSystem] = {
    ports.map { port =>
      val config = ConfigFactory
        .parseString(
          s"""akka.remote.artery.canonical.port = $port"""
        )
        .withFallback(configuration.underlying)

      ActorSystem(config.getString("play.akka.actor-system"), config)
    }
  }

  applicationLifecycle.addStopHook { () =>
    Future.successful(systems.foreach(_.terminate()))
  }
}
