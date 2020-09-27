package api.v1.account.controllers

import api.v1.account.models.User
import api.v1.account.resourcehandlers.{UserResource, UserResourceHandler}
import api.v1.common.{RequestMarkerContext, SESSION_ID, SaifuDefaultActionBuilder, SessionGenerator}
import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

class UserController @Inject() (cc: UserControllerComponents, sessionGenerator: SessionGenerator)(implicit
    ec: ExecutionContext
) extends UserBaseController(cc) {

  private val logger = Logger(getClass)

  def process: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("process: ")
      User.createUserInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          userResourceHandler.create(success).map { _ =>
            Created
          }
        }
      )
    }

  def delete: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("delete: ")
      User.deleteUserInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          userResourceHandler.delete(success).map { _ =>
            NoContent
          }
        }
      )
    }

  def login: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("login: ")
      User.loginInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          userResourceHandler.login(success).map {
            case user @ Some(UserResource(id, tenantID, roleID, loginID, name, eMail)) =>
              // Get Session ID and Encrypted Cookie
              val f = sessionGenerator
                .createSession(UserResource(id, tenantID, roleID, loginID, name, eMail))
              // TODO Delete JSON Output, UserResource must be encrypted.
              Await.ready(f, Duration.Inf)
              f.value.get match {
                case Success(sessionData) =>
                  Ok(Json.toJson(user))
                    .withSession(request.session + (SESSION_ID -> sessionData._1))
                    .withCookies(sessionData._2)
                case Failure(ex) =>
                  logger.error(ex.getMessage)
                  Unauthorized
              }
            case _ => Unauthorized
          }
        }
      )
    }
}

case class UserControllerComponents @Inject() (
    saifuDefaultActionBuilder: SaifuDefaultActionBuilder,
    userResourceHandler: UserResourceHandler,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: ExecutionContext
) extends ControllerComponents

class UserBaseController @Inject() (ucc: UserControllerComponents) extends BaseController with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = ucc

  def saifuDefaultAction: SaifuDefaultActionBuilder = ucc.saifuDefaultActionBuilder

  def userResourceHandler: UserResourceHandler = ucc.userResourceHandler
}
