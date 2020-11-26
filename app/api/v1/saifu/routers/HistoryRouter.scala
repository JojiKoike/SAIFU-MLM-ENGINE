package api.v1.saifu.routers

import api.v1.saifu.controllers.HistoryController
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class HistoryRouter @Inject() (controller: HistoryController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/$target/$dateFrom/$dateTo") =>
      controller.show(
        target = target,
        dateFrom = DateTime.parse(dateFrom),
        dateTo = DateTime.parse(dateTo)
      )
    case GET(p"/Saifu/$saifuID/$dateFrom/$dateTo") =>
      controller.show("Saifu", saifuID, DateTime.parse(dateFrom), DateTime.parse(dateTo))
  }

}
