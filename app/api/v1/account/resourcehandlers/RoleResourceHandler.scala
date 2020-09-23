package api.v1.account.resourcehandlers

import api.v1.account.models.{CreateRoleInput, DeleteRoleInput, UpdateRoleInput}
import com.saifu_mlm.engine.account.{Role, RoleDAO}
import javax.inject.Inject
import org.joda.time.DateTime
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
    val data = Role(
      java.util.UUID.randomUUID(),
      createRoleInput.name,
      Option(createRoleInput.explain),
      delete_flag = false,
      DateTime.now(),
      None
    )
    roleDAO.create(data).map { _ => createRoleResource(data) }
  }

  def lookup(id: String)(implicit mc: MarkerContext): Future[Option[RoleResource]] = {
    val roleFuture = roleDAO.lookUp(id)
    roleFuture.map { maybeRoleData =>
      maybeRoleData.map { roleData =>
        createRoleResource(roleData)
      }
    }
  }

  def update(updateRoleInput: UpdateRoleInput)(implicit mc: MarkerContext): Future[Option[RoleResource]] = {
    val roleFuture = roleDAO.lookUp(updateRoleInput.id)
    roleFuture.map { maybeRoleData =>
      maybeRoleData.map { roleData =>
        val modRoleData = roleData.copy(name = updateRoleInput.name, explain = Option(updateRoleInput.explain))
        roleDAO.update(modRoleData)
        createRoleResource(modRoleData)
      }
    }
  }

  def delete(deleteRoleInput: DeleteRoleInput)(implicit mc: MarkerContext): Future[Int] = {
    roleDAO.delete(deleteRoleInput.id)
  }

  def find(implicit mc: MarkerContext): Future[Iterable[RoleResource]] = {
    roleDAO.all.map { roleDataList =>
      roleDataList.map(roleData => createRoleResource(roleData))
    }
  }

  private def createRoleResource(role: Role): RoleResource = {
    RoleResource(role.id.toString, role.name, role.explain)
  }

}
