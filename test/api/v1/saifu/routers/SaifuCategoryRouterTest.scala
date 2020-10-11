package api.v1.saifu.routers

import api.v1.account.resourcehandlers.{RoleResource, TenantResource}
import api.v1.saifu.resourcehandlers.{SaifuCategoryResource, SaifuMainCategoryResource, SaifuSubCategoryResource}
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
    val tenantName    = "SaifuCategoryTestTenant"
    val tenantExplain = "SaifuCategoryTestTenantExplain"
    val tenantRequest = FakeRequest(POST, "/v1/account/tenant/")
      .withHeaders(HOST -> "localhost:9000")
      .withJsonBody(Json.obj("name" -> tenantName, "explain" -> tenantExplain))
      .withCSRFToken
    val tenantResult: Future[Result] = route(app, tenantRequest).get
    val tenant: TenantResource       = Json.fromJson[TenantResource](contentAsJson(tenantResult)).get
    tenantID = tenant.id
    // Role
    var roleID      = ""
    val roleName    = "SaifuCategoryTestUserRole"
    val roleExplain = "SaifuCategoryTestUserTestRoleExplain"
    val roleRequest = FakeRequest(POST, "/v1/account/role/")
      .withHeaders(HOST -> "localhost:9000")
      .withJsonBody(Json.obj("name" -> roleName, "explain" -> roleExplain))
      .withCSRFToken
    val roleResult: Future[Result] = route(app, roleRequest).get
    val role: RoleResource         = Json.fromJson[RoleResource](contentAsJson(roleResult)).get
    roleID = role.id
    // User
    val loginID  = "SaifuCategoryTestUser"
    val password = "SaifuCategoryTestUserPassword"
    val name     = "TestUserName"
    val eMail    = "saifucategorytestuser@test.com"
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
    var mainCategoryID = ""
    var subCategoryID  = ""
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
        val mainCategory: SaifuMainCategoryResource =
          Json.fromJson[SaifuMainCategoryResource](contentAsJson(result)).get
        status(result) mustBe CREATED
        mainCategoryID = mainCategory.id
      }
      "Create Sub Category" in {
        val request = FakeRequest(POST, "/v1/saifu/category/sub/")
          .withHeaders(HOST -> "localhost:9000")
          .withCookies(cookie)
          .withSession(session.data.head)
          .withJsonBody(
            Json.obj(
              "mainCategoryID" -> mainCategoryID,
              "name"           -> "TestSubCategory",
              "explain"        -> "TestSubCategoryExplain"
            )
          )
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        val subCategory: SaifuSubCategoryResource =
          Json.fromJson[SaifuSubCategoryResource](contentAsJson(result)).get
        status(result) mustBe CREATED
        subCategoryID = subCategory.id
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
              "id"      -> mainCategoryID,
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
              "id"             -> subCategoryID,
              "mainCategoryID" -> mainCategoryID,
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
        val request = FakeRequest(GET, s"/v1/saifu/category/$mainCategoryID/")
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
