package api.v1.common

import api.v1.account.resourcehandlers.UserResource
import javax.inject.{Inject, Singleton}
import play.api.http.SecretConfiguration
import play.api.mvc.Cookie
import services.encryption.{EncryptedCookieBaker, EncryptionService}
import services.session.SessionService

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

@Singleton
class UserResourceCookieBakerFactory @Inject() (
    encryptionService: EncryptionService,
    secretConfiguration: SecretConfiguration
) {

  def createCookieBaker(secretKey: Array[Byte]): EncryptedCookieBaker[UserResource] = {
    new EncryptedCookieBaker[UserResource](secretKey, encryptionService, secretConfiguration) {
      // Set session expiration duration and cookie name
      override def expirationDate: FiniteDuration = 1.hours
      override def COOKIE_NAME: String            = SESSION_DATA_COOKIE_NAME
    }
  }
}

@Singleton
class SessionGenerator @Inject() (
    sessionService: SessionService,
    encryptionService: EncryptionService,
    userResourceCookieBakerFactory: UserResourceCookieBakerFactory
)(implicit ec: ExecutionContext) {

  def createSession(userResource: UserResource): Future[(String, Cookie)] = {
    // Create UserResource Cookie encrypted with this specific secret key
    val secretKey          = encryptionService.newSecretKey
    val cookieBaker        = userResourceCookieBakerFactory.createCookieBaker(secretKey)
    val userResourceCookie = cookieBaker.encodeAsCookie(Some(userResource))

    // Create New Session Id and tie it to encrypted UserResource data and then,
    // store the pair in client side cookie
    sessionService
      .create(secretKey)
      .map(sessionId => (sessionId, userResourceCookie))
  }
}
