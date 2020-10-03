package api.v1.saifu.routers

import api.v1.saifu.controllers.SaifuCategoryController
import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class SaifuCategoryRouter @Inject() (controller: SaifuCategoryController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case GET(p"/$id") =>
      controller.show(id)

    case POST(p"/main") =>
      controller.processMain

    case PUT(p"/main") =>
      controller.updateMain

    case POST(p"/sub") =>
      controller.processSub

    case PUT(p"/sub") =>
      controller.updateSub
  }
}
