import com.google.inject.AbstractModule
import com.saifu_mlm.engine.account.{RoleDAO, TenantDAO, UserDAO}
import com.saifu_mlm.engine.account.slick.{SlickRoleDAO, SlickTenantDAO, SlickUserDAO}
import com.saifu_mlm.engine.saifu.{
  SaifuDAO,
  SaifuHistoryDAO,
  SaifuMainCategoryDAO,
  SaifuSubCategoryDAO,
  SaifuTransferDAO
}
import com.saifu_mlm.engine.saifu.slick.{
  SlickSaifuDAO,
  SlickSaifuHistoryDAO,
  SlickSaifuMainCategoryDAO,
  SlickSaifuSubCategoryDAO,
  SlickSaifuTransferDAO
}
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
    bind(classOf[UserDAO]).to(classOf[SlickUserDAO])
    bind(classOf[SaifuMainCategoryDAO]).to(classOf[SlickSaifuMainCategoryDAO])
    bind(classOf[SaifuSubCategoryDAO]).to(classOf[SlickSaifuSubCategoryDAO])
    bind(classOf[SaifuDAO]).to(classOf[SlickSaifuDAO])
    bind(classOf[SaifuTransferDAO]).to(classOf[SlickSaifuTransferDAO])
    bind(classOf[SaifuHistoryDAO]).to(classOf[SlickSaifuHistoryDAO])
    bind(classOf[DAOCloseHook]).asEagerSingleton()
    bind(classOf[ClusterSystem]).asEagerSingleton()
    bindTypedActor(SessionCache(), "replicatedCache")
  }
}
@Singleton
class DatabaseProvider @Inject() (config: Config) extends Provider[Database] {
  lazy val get = Database.forConfig("saifu_mlm_engine.database", config)
}

class DAOCloseHook @Inject() (
    tenantDAO: TenantDAO,
    roleDAO: RoleDAO,
    userDAO: UserDAO,
    saifuMainCategoryDAO: SaifuMainCategoryDAO,
    saifuSubCategoryDAO: SaifuSubCategoryDAO,
    saifuDAO: SaifuDAO,
    saifuTransferDAO: SaifuTransferDAO,
    saifuHistoryDAO: SaifuHistoryDAO,
    lifeCycle: ApplicationLifecycle
) {
  lifeCycle.addStopHook { () =>
    Future.successful {
      tenantDAO.close()
      roleDAO.close()
      userDAO.close()
      saifuMainCategoryDAO.close()
      saifuSubCategoryDAO.close()
      saifuDAO.close()
      saifuTransferDAO.close()
      saifuHistoryDAO.close()
    }
  }
}
