package api.v1.saifu.routers

import api.v1.account.resourcehandlers.{RoleResource, TenantResource}
import api.v1.saifu.resourcehandlers.{SaifuMainCategoryResource, SaifuResource, SaifuSubCategoryResource}
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

class SaifuRouterTest extends PlaySpec with GuiceOneAppPerSuite {

  "SaifuRouter" should {

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
    // Create Saifu Main Category
    var saifuMainCategoryID = ""
    var saifuSubCategoryID  = ""
    val saifuMainCategoryCreateRequest = FakeRequest(POST, "/v1/saifu/category/main/")
      .withHeaders(HOST -> "localhost:9000")
      .withCookies(cookie)
      .withSession(session.data.head)
      .withJsonBody(
        Json.obj(
          "name"    -> "SaifuTestMainCategory",
          "explain" -> "SaifuTestMainCategoryExplain"
        )
      )
      .withCSRFToken
    val resultSaifuMainCategoryCreate: Future[Result] = route(app, saifuMainCategoryCreateRequest).get
    val mainCategory: SaifuMainCategoryResource =
      Json.fromJson[SaifuMainCategoryResource](contentAsJson(resultSaifuMainCategoryCreate)).get
    saifuMainCategoryID = mainCategory.id
    val saifuSubCategoryCreateRequest = FakeRequest(POST, "/v1/saifu/category/sub/")
      .withHeaders(HOST -> "localhost:9000")
      .withCookies(cookie)
      .withSession(session.data.head)
      .withJsonBody(
        Json.obj(
          "mainCategoryID" -> saifuMainCategoryID,
          "name"           -> "SaifuTestSubCategory",
          "explain"        -> "SaifuTestSubCategoryExplain"
        )
      )
      .withCSRFToken
    val resultSaifuSubCategoryCreate: Future[Result] = route(app, saifuSubCategoryCreateRequest).get
    val subCategory: SaifuSubCategoryResource =
      Json.fromJson[SaifuSubCategoryResource](contentAsJson(resultSaifuSubCategoryCreate)).get
    saifuSubCategoryID = subCategory.id

    var saifuID = ""
    "Saifu CRU Should" should {

      val saifuName      = "TestSaifu"
      val saifuExplain   = "TestSaifuExplain"
      val initialBalance = 1000000

      "Create Saifu" in {
        val request = FakeRequest(POST, "/v1/saifu/")
          .withHeaders(HOST -> "localhost:9000")
          .withCookies(cookie)
          .withSession(session.data.head)
          .withJsonBody(
            Json.obj(
              "subCategoryID"  -> saifuSubCategoryID,
              "name"           -> saifuName,
              "explain"        -> saifuExplain,
              "initialBalance" -> initialBalance
            )
          )
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        status(result) mustBe CREATED
      }

      "Read All My Saifu" in {
        val request = FakeRequest(GET, "/v1/saifu/")
          .withHeaders(HOST -> "localhost:9000")
          .withCookies(cookie)
          .withSession(session.data.head)
          .withCSRFToken
        val result: Future[Result]    = route(app, request).get
        val saifu: Seq[SaifuResource] = Json.fromJson[Seq[SaifuResource]](contentAsJson(result)).get
        status(result) mustBe OK
        saifuID = saifu.head.id
      }

      "Read One of My Saifu" in {
        val request = FakeRequest(GET, s"/v1/saifu/$saifuID/")
          .withHeaders(HOST -> "localhost:9000")
          .withCookies(cookie)
          .withSession(session.data.head)
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        val saifu: SaifuResource   = Json.fromJson[SaifuResource](contentAsJson(result)).get
        status(result) mustBe OK
        saifu.name mustBe saifuName
        saifu.explain mustBe saifuExplain
        saifu.balance mustBe initialBalance
      }

      "Update Saifu" in {
        val request = FakeRequest(PUT, "/v1/saifu/")
          .withHeaders(HOST -> "localhost:9000")
          .withCookies(cookie)
          .withSession(session.data.head)
          .withJsonBody(
            Json.obj(
              "saifuID"        -> saifuID,
              "subCategoryID"  -> saifuSubCategoryID,
              "name"           -> "UpdateTestSaifu",
              "explain"        -> "UpdateTestSaifuExplain",
              "initialBalance" -> 9999999
            )
          )
          .withCSRFToken
        val result: Future[Result] = route(app, request).get
        status(result) mustBe NO_CONTENT
      }
    }
  }
}
