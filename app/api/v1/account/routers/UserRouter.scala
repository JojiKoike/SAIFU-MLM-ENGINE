package api.v1.account.routers

import api.v1.account.controllers.UserController
import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class UserRouter @Inject() (controller: UserController) extends SimpleRouter {

  override def routes: Routes = {

    case POST(p"/") =>
      controller.process

    case POST(p"/login") =>
      controller.login

    case DELETE(p"/") =>
      controller.delete

  }
}
