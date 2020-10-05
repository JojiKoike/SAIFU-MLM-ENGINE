package api.v1.saifu.routers

import api.v1.account.resourcehandlers.{RoleResource, TenantResource}
import api.v1.saifu.resourcehandlers.SaifuCategoryResource
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
    "Create Category" should {
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
      "Create Sub Category" in {
        val request = FakeRequest(POST, "/v1/saifu/category/sub/")
          .withHeaders(HOST -> "localhost:9000")
          .withCookies(cookie)
          .withSession(session.data.head)
          .withJsonBody(
            Json.obj(
              "mainCategoryID" -> "1",
              "name"           -> "TestSubCategory",
              "explain"        -> "TestSubCategoryExplain"
            )
          )
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        status(result) mustBe CREATED
      }
    }
    "Update Category" should {
      "Update Main Category" in {
        val request = FakeRequest(PUT, "/v1/saifu/category/main/")
          .withHeaders(HOST -> "localhost:9000")
          .withCookies(cookie)
          .withSession(session.data.head)
          .withJsonBody(
            Json.obj(
              "id"      -> "1",
              "name"    -> "UpdateMainCategory",
              "explain" -> "UpdateMainCategoryExplain"
            )
          )
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        status(result) mustBe NO_CONTENT
      }
      "Update Sub Category" in {
        val request = FakeRequest(PUT, "/v1/saifu/category/sub/")
          .withHeaders(HOST -> "localhost:9000")
          .withCookies(cookie)
          .withSession(session.data.head)
          .withJsonBody(
            Json.obj(
              "id"             -> "1",
              "mainCategoryID" -> "1",
              "name"           -> "UpdateSubCategory",
              "explain"        -> "UpdateSubCategoryExplain"
            )
          )
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        status(result) mustBe NO_CONTENT
      }
    }
    "Read Category" should {
      "Read One Saifu Category Data" in {
        val request = FakeRequest(GET, "/v1/saifu/category/1/")
          .withHeaders(HOST -> "localhost:9000")
          .withCookies(cookie)
          .withSession(session.data.head)
          .withCSRFToken
        val result: Future[Result]          = route(app, request).get
        val category: SaifuCategoryResource = Json.fromJson[SaifuCategoryResource](contentAsJson(result)).get
        category.mainCategory.name mustBe "UpdateMainCategory"
        category.mainCategory.explain.get mustBe "UpdateMainCategoryExplain"
        category.subCategories.head.name mustBe "UpdateSubCategory"
        category.subCategories.head.explain.get mustBe "UpdateSubCategoryExplain"
        status(result) mustBe OK
      }
      "Read All Saifu Category Date" in {
        val request = FakeRequest(GET, "/v1/saifu/category/")
          .withHeaders(HOST -> "localhost:9000")
          .withCookies(cookie)
          .withSession(session.data.head)
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        val categories: Seq[SaifuCategoryResource] =
          Json.fromJson[Seq[SaifuCategoryResource]](contentAsJson(result)).get
        categories.head.mainCategory.name mustBe "UpdateMainCategory"
        categories.head.mainCategory.explain.get mustBe "UpdateMainCategoryExplain"
        categories.head.subCategories.head.name mustBe "UpdateSubCategory"
        categories.head.subCategories.head.explain.get mustBe "UpdateSubCategoryExplain"
        status(result) mustBe OK
      }
    }
  }

}
