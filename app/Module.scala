import com.google.inject.AbstractModule
import com.saifu_mlm.engine.account.TenantDAO
import com.saifu_mlm.engine.account.slick.SlickTenantDAO
import com.typesafe.config.Config
import javax.inject.{Inject, Provider, Singleton}
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Environment}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future

class Module(environment: Environment, configuration: Configuration) extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Database]).toProvider(classOf[DatabaseProvider])
    bind(classOf[TenantDAO]).to(classOf[SlickTenantDAO])
    bind(classOf[TenantDAOCloseHook]).asEagerSingleton()
  }
}
@Singleton
class DatabaseProvider @Inject() (config: Config) extends Provider[Database] {
  lazy val get = Database.forConfig("saifu_mlm_engine.database", config)
}

class TenantDAOCloseHook @Inject() (dao: TenantDAO, lifeCycle: ApplicationLifecycle) {
  lifeCycle.addStopHook { () =>
    Future.successful(dao.close())
  }
}
