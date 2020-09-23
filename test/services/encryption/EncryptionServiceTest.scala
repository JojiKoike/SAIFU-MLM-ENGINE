package services.encryption

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{Format, Json}

case class Foo(name: String, age: Int)

object Foo {
  implicit val format: Format[Foo] = Json.format[Foo]
}

class EncryptionServiceTest extends PlaySpec with GuiceOneAppPerSuite {
  "encryption info service" should {
    "encrypt data" in {
      val service      = app.injector.instanceOf(classOf[EncryptionService])
      val secretKey    = service.newSecretKey
      val option       = Option(Foo(name = "george", age = 40))
      val encryptedMap = service.encrypt[Foo](secretKey, option)
      val decrypted    = service.decrypt[Foo](secretKey, encryptedMap)
      decrypted mustBe Some(Foo(name = "george", age = 40))
    }
  }
}
