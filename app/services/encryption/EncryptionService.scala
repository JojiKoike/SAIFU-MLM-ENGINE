package services.encryption

import java.nio.charset.StandardCharsets
import java.security.SecureRandom

import javax.inject.{Inject, Singleton}
import org.abstractj.kalium.crypto.SecretBox
import play.api.libs.json.{Json, Reads, Writes}
import play.api.{Configuration, Logger}

@Singleton
class EncryptionService @Inject() (configuration: Configuration) {
  private val random = new SecureRandom()

  private val logger = Logger(this.getClass)

  def newSecretKey: Array[Byte] = {
    // Key must be 32 bytes for secretbox
    import org.abstractj.kalium.NaCl.Sodium.CRYPTO_SECRETBOX_XSALSA20POLY1305_KEYBYTES
    val buf = new Array[Byte](CRYPTO_SECRETBOX_XSALSA20POLY1305_KEYBYTES)
    random.nextBytes(buf)
    buf
  }

  def encrypt[A: Writes](secretKey: Array[Byte], encryptTarget: Option[A]): Map[String, String] = {
    val nonce      = Nonce.createNonce()
    val json       = Json.toJson(encryptTarget)
    val stringData = Json.stringify(json)
    logger.info(s"encrypt: encryptTarget = $encryptTarget, stringData = $stringData")

    val rawData    = stringData.getBytes(StandardCharsets.UTF_8)
    val cipherText = box(secretKey).encrypt(nonce.raw, rawData)

    val nonceHex  = encoder.encode(nonce.raw)
    val cipherHex = encoder.encode(cipherText)
    Map("nonce" -> nonceHex, "c" -> cipherHex)
  }

  def decrypt[A: Reads](secretKey: Array[Byte], data: Map[String, String]): Option[A] = {
    val nonceHex      = data("nonce")
    val nonce         = Nonce.nonceFromBytes(encoder.decode(nonceHex))
    val cipherTextHex = data("c")
    val cipherText    = encoder.decode(cipherTextHex)
    val rawData       = box(secretKey).decrypt(nonce.raw, cipherText)
    val stringData    = new String(rawData, StandardCharsets.UTF_8)
    val json          = Json.parse(stringData)
    val result        = Json.fromJson[A](json).asOpt
    logger.info(s"decrypt: json = $json, result = $result")
    result
  }

  private def encoder = org.abstractj.kalium.encoders.Encoder.HEX

  private def box(secretKey: Array[Byte]) = {
    new SecretBox(secretKey)
  }
}
