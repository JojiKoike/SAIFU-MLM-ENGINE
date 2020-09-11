package v1.account

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future

class AccountRouterSpec extends PlaySpec with GuiceOneAppPerTest {

  "AccountRouter" should {

    "CRUD tenant" should {
      var id      = ""
      val name    = "TestTenant"
      val explain = "TestTenantExplain"
      "Create Tenant" in {
        val request = FakeRequest(POST, "/v1/account/tenant/")
          .withHeaders(HOST -> "localhost:9000")
          .withJsonBody(Json.obj("name" -> name, "explain" -> explain))
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        val tenant: TenantResource = Json.fromJson[TenantResource](contentAsJson(result)).get
        tenant.name mustBe name
        tenant.explain.get mustBe explain
        status(result) mustBe CREATED
        id = tenant.id
      }
      "Read Tenant" in {
        val request = FakeRequest(GET, s"/v1/account/tenant/$id")
          .withHeaders(HOST -> "localhost:9000")
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        val tenant: TenantResource = Json.fromJson[TenantResource](contentAsJson(result)).get
        tenant.name mustBe name
        tenant.explain.get mustBe explain
        status(result) mustBe OK
      }
      "Index Tenant" in {
        val request = FakeRequest(GET, "/v1/account/tenant/")
          .withHeaders(HOST -> "localhost:9000")
          .withCSRFToken
        val result: Future[Result]       = route(app, request).get
        val tenants: Seq[TenantResource] = Json.fromJson[Seq[TenantResource]](contentAsJson(result)).get
        tenants.filter(_.id == id).head mustBe TenantResource(id, name, Option(explain))
      }
      "Update Tenant" in {
        val newName    = "NewTenant"
        val newExplain = "NewTenant Explain"
        val request = FakeRequest(PUT, "/v1/account/tenant/")
          .withHeaders(HOST -> "localhost:9000")
          .withJsonBody(Json.obj("id" -> id, "name" -> newName, "explain" -> newExplain))
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        status(result) mustBe NO_CONTENT
      }
      "Delete Tenant" in {
        val request = FakeRequest(DELETE, "/v1/account/tenant/")
          .withHeaders(HOST -> "localhost:9000")
          .withJsonBody(Json.obj("id" -> id))
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        status(result) mustBe NO_CONTENT
      }
    }
  }
}
