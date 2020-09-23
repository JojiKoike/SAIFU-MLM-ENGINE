import com.google.inject.AbstractModule
import com.saifu_mlm.engine.account.{RoleDAO, TenantDAO}
import com.saifu_mlm.engine.account.slick.{SlickRoleDAO, SlickTenantDAO}
import com.typesafe.config.Config
import javax.inject.{Inject, Provider, Singleton}
import play.api.inject.ApplicationLifecycle
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}
import services.cluster.ClusterSystem
import services.session.SessionCache
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[Database]).toProvider(classOf[DatabaseProvider])
    bind(classOf[TenantDAO]).to(classOf[SlickTenantDAO])
    bind(classOf[RoleDAO]).to(classOf[SlickRoleDAO])
    bind(classOf[TenantDAOCloseHook]).asEagerSingleton()
    bind(classOf[RoleDAOCloseHook]).asEagerSingleton()
    bind(classOf[ClusterSystem]).asEagerSingleton()
    bindTypedActor(SessionCache(), "replicatedCache")
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

class RoleDAOCloseHook @Inject() (dao: RoleDAO, lifeCycle: ApplicationLifecycle) {
  lifeCycle.addStopHook { () =>
    Future.successful(dao.close())
  }
}
