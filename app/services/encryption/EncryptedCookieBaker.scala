package services.encryption

import play.api.http.{JWTConfiguration, SecretConfiguration}
import play.api.libs.json.Format
import play.api.mvc.{CookieBaker, JWTCookieDataCodec}

import scala.concurrent.duration.FiniteDuration

abstract class EncryptedCookieBaker[A: Format](
    secretKey: Array[Byte],
    encryptionService: EncryptionService,
    val secretConfiguration: SecretConfiguration
) extends CookieBaker[Option[A]]
    with JWTCookieDataCodec {

  def expirationDate: FiniteDuration

  def COOKIE_NAME: String

  override val isSigned               = true
  override val path: String           = "/"
  override val emptyCookie: Option[A] = None

  override lazy val maxAge: Option[Int] = Option(expirationDate).map(_.toSeconds.toInt)

  override lazy val jwtConfiguration: JWTConfiguration = JWTConfiguration(expiresAfter = Some(expirationDate))

  override protected def serialize(jsonClass: Option[A]): Map[String, String] = {
    encryptionService.encrypt(secretKey, jsonClass)
  }

  override protected def deserialize(stringMap: Map[String, String]): Option[A] = {
    encryptionService.decrypt(secretKey, stringMap)
  }

}
