package api.v1.account.routers

import api.v1.account.controllers.RoleController
import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class RoleRouter @Inject() (controller: RoleController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case GET(p"/$id") =>
      controller.show(id)

    case POST(p"/") =>
      controller.process

    case PUT(p"/") =>
      controller.update

    case DELETE(p"/") =>
      controller.delete
  }
}
