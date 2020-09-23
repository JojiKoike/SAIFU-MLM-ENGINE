package api.v1.account.routers

import java.io.File

import api.v1.account.resourcehandlers.RoleResource
import com.typesafe.config.ConfigFactory
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future

class RoleRouterTest extends PlaySpec with GuiceOneAppPerSuite {
  "RoleRouter" should {
    val conf = ConfigFactory.parseFile(new File())
    "CRUD role" should {
      var id      = ""
      val name    = "TestRole"
      val explain = "TestRoleExplain"
      "Create Role" in {
        val request = FakeRequest(POST, "/v1/account/role/")
          .withHeaders(HOST -> "localhost:9000")
          .withJsonBody(Json.obj("name" -> name, "explain" -> explain))
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        val role: RoleResource     = Json.fromJson[RoleResource](contentAsJson(result)).get
        role.name mustBe name
        role.explain.get mustBe explain
        status(result) mustBe CREATED
        id = role.id
      }
      "Read Role" in {
        val request = FakeRequest(GET, s"/v1/account/role/$id")
          .withHeaders(HOST -> "localhost:9000")
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        val tenant: RoleResource   = Json.fromJson[RoleResource](contentAsJson(result)).get
        tenant.name mustBe name
        tenant.explain.get mustBe explain
        status(result) mustBe OK
      }
      "Index Role" in {
        val request = FakeRequest(GET, "/v1/account/role/")
          .withHeaders(HOST -> "localhost:9000")
          .withCSRFToken
        val result: Future[Result]   = route(app, request).get
        val roles: Seq[RoleResource] = Json.fromJson[Seq[RoleResource]](contentAsJson(result)).get
        roles.filter(_.id == id).head mustBe RoleResource(id, name, Option(explain))
        status(result) mustBe OK
      }
      "Update Role" in {
        val newName    = "NewRole"
        val newExplain = "NewRole Explain"
        val request = FakeRequest(PUT, "/v1/account/role/")
          .withHeaders(HOST -> "localhost:9000")
          .withJsonBody(Json.obj("id" -> id, "name" -> newName, "explain" -> newExplain))
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        status(result) mustBe NO_CONTENT
      }
      "Delete Role" in {
        val request = FakeRequest(DELETE, "/v1/account/role/")
          .withHeaders(HOST -> "localhost:9000")
          .withJsonBody(Json.obj("id" -> id))
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        status(result) mustBe NO_CONTENT
      }
    }
  }
}
