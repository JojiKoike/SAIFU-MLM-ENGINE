package api.v1.account.resourcehandlers

import api.v1.account.models.{CreateUserInput, DeleteRoleInput, DeleteUserInput, LoginInput}
import com.saifu_mlm.engine.account.{User, UserDAO}
import javax.inject.Inject
import org.mindrot.jbcrypt.BCrypt
import play.api.MarkerContext
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

case class UserResource(id: String, tenantID: String, roleID: String, loginID: String, name: String, eMail: String)

object UserResource {
  implicit val format: Format[UserResource] = Json.format
}

class UserResourceHandler @Inject() (
    userDAO: UserDAO
)(implicit ec: ExecutionContext) {

  def create(createUserInput: CreateUserInput)(implicit mc: MarkerContext): Future[Int] = {
    val data = User(
      None,
      tenantId = createUserInput.tenantId,
      roleId = createUserInput.roleId,
      loginId = createUserInput.loginId,
      name = createUserInput.name,
      password = BCrypt.hashpw(createUserInput.password, BCrypt.gensalt()),
      eMail = createUserInput.eMail,
      None,
      None
    )
    userDAO.create(data)
  }

  def delete(deleteUserInput: DeleteUserInput)(implicit mc: MarkerContext): Future[Int] = {
    userDAO.delete(deleteUserInput.id)
  }

  def login(loginInput: LoginInput)(implicit mc: MarkerContext): Future[Option[UserResource]] = {
    userDAO.login(loginInput.loginID).map { maybeUserData =>
      maybeUserData.map { userData =>
        {
          if (BCrypt.checkpw(loginInput.password, userData.password)) {
            println(s"Success!! : ${loginInput.password} : ${userData.password}")
            createUserResource(userData)
          } else {
            null
          }
        }
      }
    }
  }

  private def createUserResource(user: User): UserResource = {
    UserResource(user.id.get, user.tenantId, user.roleId, user.loginId, user.name, user.eMail)
  }
}
