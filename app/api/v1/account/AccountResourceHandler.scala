package api.v1.account

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
  def create(createTenantInput: CreateTenantInput)(implicit mc: MarkerContext): Future[TenantResource] = {
    val data = Tenant(
      java.util.UUID.randomUUID(),
      createTenantInput.name,
      Option(createTenantInput.explain),
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

  def update(updateTenantInput: UpdateTenantInput)(implicit mc: MarkerContext): Future[Option[TenantResource]] = {
    val tenantFuture = tenantDAO.lookUp(updateTenantInput.id)
    tenantFuture.map { maybeTenantData =>
      maybeTenantData.map { tenantData =>
        val modTenantData = tenantData.copy(name = updateTenantInput.name, explain = Option(updateTenantInput.explain))
        tenantDAO.update(modTenantData)
        createTenantResource(modTenantData)
      }
    }
  }

  def delete(deleteTenantInput: DeleteTenantInput)(implicit mc: MarkerContext): Future[Int] = {
    tenantDAO.delete(deleteTenantInput.id)
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
