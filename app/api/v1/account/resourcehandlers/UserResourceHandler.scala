package api.v1.account.resourcehandlers

import api.v1.account.models.{CreateUserInput, DeleteUserInput, LoginInput}
import api.v1.common.ERROR_CODE
import com.github.t3hnar.bcrypt._
import com.saifu_mlm.engine.account.{User, UserDAO}
import javax.inject.Inject
import play.api.libs.json.{Format, Json}
import play.api.{Logger, MarkerContext}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class UserResource(id: String, tenantID: String, roleID: String, loginID: String, name: String, eMail: String)

object UserResource {
  implicit val format: Format[UserResource] = Json.format
}

class UserResourceHandler @Inject() (
    userDAO: UserDAO
)(implicit ec: ExecutionContext) {

  private val logger = Logger(getClass)

  def create(createUserInput: CreateUserInput)(implicit mc: MarkerContext): Future[Int] = {
    createUserInput.password.bcryptSafeBounded match {
      case Success(encryptedPassword) =>
        val data = User(
          None,
          tenantId = createUserInput.tenantId,
          roleId = createUserInput.roleId,
          loginId = createUserInput.loginId,
          name = createUserInput.name,
          password = encryptedPassword,
          eMail = createUserInput.eMail,
          None,
          None
        )
        userDAO.create(data)
      case Failure(exception) =>
        logger.error(exception.getMessage)
        Future(ERROR_CODE)
    }
  }

  def delete(deleteUserInput: DeleteUserInput)(implicit mc: MarkerContext): Future[Int] = {
    userDAO.delete(deleteUserInput.id)
  }

  def login(loginInput: LoginInput)(implicit mc: MarkerContext): Future[Option[UserResource]] = {
    userDAO.login(loginInput.loginID).map { maybeUserData =>
      maybeUserData.map { userData =>
        {
          loginInput.password.isBcryptedSafeBounded(userData.password) match {
            case Success(true) => createUserResource(userData)
            case Failure(exception) => {
              logger.error(exception.getMessage)
              // TODO Mod Return Value for null safe
              null
            }
          }
        }
      }
    }
  }

  private def createUserResource(user: User): UserResource = {
    UserResource(user.id.get, user.tenantId, user.roleId, user.loginId, user.name, user.eMail)
  }
}
