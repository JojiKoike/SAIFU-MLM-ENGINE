package api.v1.saifu.routers

import api.v1.saifu.controllers.SaifuController
import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class SaifuRouter @Inject() (controller: SaifuController) extends SimpleRouter {
  override def routes: Routes = {
    case (p"/") =>
      controller.index

    case GET(p"/$id") =>
      controller.show(id)

    case POST(p"/") =>
      controller.process

    case PUT(p"/") =>
      controller.update
  }
}
