package api.v1.saifu.routers

import api.v1.saifu.controllers.TransferController
import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class TransferRouter @Inject() (controller: TransferController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case GET(p"/$saifuID") =>
      controller.show(saifuID)

    case POST(p"/") =>
      controller.process

    case PUT(p"/") =>
      controller.update
  }
}
