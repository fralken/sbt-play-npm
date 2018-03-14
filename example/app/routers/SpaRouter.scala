package routers

import javax.inject.Inject

import controllers.Assets
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class SpaRouter @Inject()(assets: Assets) extends SimpleRouter
{
  override def routes: Routes = {
    case GET(p"/") => assets.at("index.html")
    case GET(p"/$file<[^.]+([.][^.]+)+>") => assets.at(file)
    case GET(p"/$route*") => assets.at("index.html")
  }
}
