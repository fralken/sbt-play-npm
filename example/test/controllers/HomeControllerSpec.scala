package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.JsString
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "HomeController GET" should {

    "return a json from a new instance of controller" in {
      val controller = new HomeController(stubControllerComponents())
      val home = controller.greetings().apply(FakeRequest(GET, "/api/greetings"))

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsJson(home) mustEqual JsString("Welcome to sbt-play-npm!")
    }

    "return a json from the application" in {
      val controller = inject[HomeController]
      val home = controller.greetings().apply(FakeRequest(GET, "/api/greetings"))

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsJson(home) mustEqual JsString("Welcome to sbt-play-npm!")
    }

    "return a json from the router" in {
      val request = FakeRequest(GET, "/api/greetings")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsJson(home) mustEqual JsString("Welcome to sbt-play-npm!")
    }
  }
}
