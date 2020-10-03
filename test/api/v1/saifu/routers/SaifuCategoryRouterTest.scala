package api.v1.saifu.routers

import api.v1.account.resourcehandlers.{RoleResource, TenantResource}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.{Cookie, Result, Session}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.Success

class SaifuCategoryRouterTest extends PlaySpec with GuiceOneAppPerSuite {

  "SaifuCategoryRouter" should {
    "Create Category" should {
      // Prepare Dummy Tenant and Role
      // Tenant
      var tenantID      = ""
      val tenantName    = "SaifuTestTenant"
      val tenantExplain = "SaifuTestTenantExplain"
      val tenantRequest = FakeRequest(POST, "/v1/account/tenant/")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(Json.obj("name" -> tenantName, "explain" -> tenantExplain))
        .withCSRFToken
      val tenantResult: Future[Result] = route(app, tenantRequest).get
      val tenant: TenantResource       = Json.fromJson[TenantResource](contentAsJson(tenantResult)).get
      tenantID = tenant.id
      // Role
      var roleID      = ""
      val roleName    = "SaifuUserRole"
      val roleExplain = "UserTestRoleExplain"
      val roleRequest = FakeRequest(POST, "/v1/account/role/")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(Json.obj("name" -> roleName, "explain" -> roleExplain))
        .withCSRFToken
      val roleResult: Future[Result] = route(app, roleRequest).get
      val role: RoleResource         = Json.fromJson[RoleResource](contentAsJson(roleResult)).get
      roleID = role.id
      // User
      val loginID  = "SaifuTestUser"
      val password = "SaifuTestUserPassword"
      val name     = "TestUserName"
      val eMail    = "saifutestuser@test.com"
      val userRequest = FakeRequest(POST, "/v1/account/user/")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(
          Json.obj(
            "tenantId" -> tenantID,
            "roleId"   -> roleID,
            "loginId"  -> loginID,
            "name"     -> name,
            "password" -> password,
            "eMail"    -> eMail
          )
        )
        .withCSRFToken
      val result: Future[Result] = route(app, userRequest).get
      Await.ready(result, Duration.Inf)
      // Login
      var cookie: Cookie   = Cookie(name = "", value = "")
      var session: Session = Session()
      val requestLogin = FakeRequest(POST, "/v1/account/user/login/")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(
          Json.obj(
            "loginID"  -> loginID,
            "password" -> password
          )
        )
        .withCSRFToken
      val resultLogin: Future[Result] = route(app, requestLogin).get
      Await.ready(resultLogin, Duration.Inf)
      resultLogin.value.get match {
        case Success(result) =>
          cookie = result.newCookies.head
          session = result.newSession.get
      }
      "Create Main Category" in {
        val request = FakeRequest(POST, "/v1/saifu/category/main/")
          .withHeaders(HOST -> "localhost:9000")
          .withCookies(cookie)
          .withSession(session.data.head)
          .withJsonBody(
            Json.obj(
              "name"    -> "TestMainCategory",
              "explain" -> "TestMainCategoryExplain"
            )
          )
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        status(result) mustBe CREATED
      }
    }

  }

}
