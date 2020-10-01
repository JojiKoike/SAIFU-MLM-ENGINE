package api.v1.account.resourcehandlers

import java.util.UUID

import api.v1.account.models.{CreateRoleInput, DeleteRoleInput, UpdateRoleInput}
import com.saifu_mlm.engine.account.{Role, RoleDAO}
import javax.inject.Inject
import play.api.MarkerContext
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

case class RoleResource(id: String, name: String, explain: Option[String])

object RoleResource {
  implicit val format: Format[RoleResource] = Json.format
}

class RoleResourceHandler @Inject() (
    roleDAO: RoleDAO
)(implicit ec: ExecutionContext) {
  def create(createRoleInput: CreateRoleInput)(implicit mc: MarkerContext): Future[RoleResource] = {
    roleDAO
      .create(Role(UUID.randomUUID.toString, createRoleInput.name, Option(createRoleInput.explain)))
      .map(createRoleResource)
  }

  def lookup(id: String)(implicit mc: MarkerContext): Future[Option[RoleResource]] = {
    roleDAO
      .lookUp(id)
      .map(maybeRoleData => maybeRoleData.map(createRoleResource))
  }

  def update(updateRoleInput: UpdateRoleInput)(implicit mc: MarkerContext): Future[Int] = {
    roleDAO
      .update(Role(updateRoleInput.id, updateRoleInput.name, Option(updateRoleInput.explain)))
  }

  def delete(deleteRoleInput: DeleteRoleInput)(implicit mc: MarkerContext): Future[Int] = {
    roleDAO
      .delete(deleteRoleInput.id)
  }

  def find(implicit mc: MarkerContext): Future[Iterable[RoleResource]] = {
    roleDAO.all.map { roleDataList =>
      roleDataList.map(roleData => createRoleResource(roleData))
    }
  }

  private def createRoleResource(role: Role): RoleResource = {
    RoleResource(role.id, role.name, role.explain)
  }

}
