package v1.account

import com.saifu_mlm.engine.account.{Tenant, TenantDAO}
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.MarkerContext
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

case class TenantResource(id: String, name: String, explain: Option[String])

object TenantResource {

  implicit val format: Format[TenantResource] = Json.format
}

class TenantResourceHandler @Inject() (
    tenantDAO: TenantDAO
)(implicit ec: ExecutionContext) {
  def create(tenantFormInput: TenantFormInput)(implicit mc: MarkerContext): Future[TenantResource] = {
    val data = Tenant(
      java.util.UUID.randomUUID(),
      tenantFormInput.name,
      Option(tenantFormInput.explain),
      delete_flag = false,
      DateTime.now(),
      None
    )
    tenantDAO.create(data).map { _ =>
      createTenantResource(data)
    }
  }

  def lookup(id: String)(implicit
      mc: MarkerContext
  ): Future[Option[TenantResource]] = {
    val tenantFuture = tenantDAO.lookUp(id)
    tenantFuture.map { maybeTenantData =>
      maybeTenantData.map { tenantData =>
        createTenantResource(tenantData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[TenantResource]] = {
    tenantDAO.all.map { tenantDataList =>
      tenantDataList.map(tenantData => createTenantResource(tenantData))
    }
  }

  private def createTenantResource(tenant: Tenant): TenantResource = {
    TenantResource(tenant.id.toString, tenant.name, tenant.explain)
  }

}
