package v1.account

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class AccountRouter @Inject() (controller: TenantController) extends SimpleRouter {

  override def routes: Routes = {
    case GET(p"/tenant/") =>
      controller.index
    case POST(p"/tenant/") =>
      controller.process
    case GET(p"/tenant/$id") =>
      controller.show(id)
  }
}
