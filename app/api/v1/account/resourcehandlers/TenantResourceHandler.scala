package api.v1.account.resourcehandlers

import java.util.UUID

import api.v1.account.models.{CreateTenantInput, DeleteTenantInput, UpdateTenantInput}
import com.saifu_mlm.engine.account.{Tenant, TenantDAO}
import javax.inject.Inject
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
    tenantDAO
      .create(Tenant(UUID.randomUUID.toString, createTenantInput.name, Option(createTenantInput.explain)))
      .map(createTenantResource)
  }

  def lookup(id: String)(implicit
      mc: MarkerContext
  ): Future[Option[TenantResource]] = {
    tenantDAO
      .lookUp(id)
      .map(maybeTenantData => maybeTenantData.map(createTenantResource))
  }

  def update(updateTenantInput: UpdateTenantInput)(implicit mc: MarkerContext): Future[Int] = {
    tenantDAO
      .update(Tenant(updateTenantInput.id, updateTenantInput.name, Option(updateTenantInput.explain)))
  }

  def delete(deleteTenantInput: DeleteTenantInput)(implicit mc: MarkerContext): Future[Int] = {
    tenantDAO
      .delete(deleteTenantInput.id)
  }

  def find(implicit mc: MarkerContext): Future[Iterable[TenantResource]] = {
    tenantDAO.all.map { tenantDataList =>
      tenantDataList.map(tenantData => createTenantResource(tenantData))
    }
  }

  private def createTenantResource(tenant: Tenant): TenantResource = {
    TenantResource(tenant.id, tenant.name, tenant.explain)
  }

}
