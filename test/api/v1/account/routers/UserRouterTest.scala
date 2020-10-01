package api.v1.account.routers

import api.v1.account.resourcehandlers.{RoleResource, TenantResource, UserResource}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Success

class UserRouterTest extends PlaySpec with GuiceOneAppPerSuite {
  "UserRouter" should {
    "CRUD User" should {
      // Prepare Dummy Tenant and Role
      // Tenant
      var tenantID      = ""
      val tenantName    = "UserTenant"
      val tenantExplain = "UserTestTenantExplain"
      val tenantRequest = FakeRequest(POST, "/v1/account/tenant/")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(Json.obj("name" -> tenantName, "explain" -> tenantExplain))
        .withCSRFToken
      val tenantResult: Future[Result] = route(app, tenantRequest).get
      val tenant: TenantResource       = Json.fromJson[TenantResource](contentAsJson(tenantResult)).get
      tenantID = tenant.id
      // Role
      var roleID      = ""
      val roleName    = "UserRole"
      val roleExplain = "UserTestRoleExplain"
      val roleRequest = FakeRequest(POST, "/v1/account/role/")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(Json.obj("name" -> roleName, "explain" -> roleExplain))
        .withCSRFToken
      val roleResult: Future[Result] = route(app, roleRequest).get
      val role: RoleResource         = Json.fromJson[RoleResource](contentAsJson(roleResult)).get
      roleID = role.id

      val loginID  = "TestUser"
      val password = "TestUserPassword"
      var userID   = ""
      "Create User" in {
        val name  = "TestUserName"
        val eMail = "testuser@test.com"
        val request = FakeRequest(POST, "/v1/account/user/")
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
        val result: Future[Result] = route(app, request).get
        status(result) mustBe CREATED
      }

      "Login" in {
        val request = FakeRequest(POST, "/v1/account/user/login/")
          .withHeaders(HOST -> "localhost:9000")
          .withJsonBody(
            Json.obj(
              "loginID"  -> loginID,
              "password" -> password
            )
          )
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        Await.ready(result, Duration.Inf)
        result.value.get match {
          case Success(result) =>
            println(result.newSession.get.data)
            println(result.newCookies.head.name)
            println(result.newCookies.head.value)
        }
        val user: UserResource = Json.fromJson[UserResource](contentAsJson(result)).get
        status(result) mustBe OK
        userID = user.id
      }

      "Delete User" in {
        val request = FakeRequest(DELETE, "/v1/account/user/")
          .withHeaders(HOST -> "localhost:9000")
          .withJsonBody(
            Json.obj(
              "id" -> userID
            )
          )
        val result: Future[Result] = route(app, request).get
        status(result) mustBe NO_CONTENT
        // Delete Dummy Tenant and Role
        route(
          app,
          FakeRequest(DELETE, "/v1/account/tenant/")
            .withHeaders(HOST -> "localhost:9000")
            .withJsonBody(Json.obj("id" -> tenantID))
            .withCSRFToken
        ).get
        route(
          app,
          FakeRequest(DELETE, "/v1/account/role/")
            .withHeaders(HOST -> "localhost:9000")
            .withJsonBody(Json.obj("id" -> roleID))
            .withCSRFToken
        ).get
      }
    }
  }
}
