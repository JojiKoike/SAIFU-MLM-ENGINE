package services.encryption

import org.abstractj.kalium.crypto.Random

/**
  * Nonces are used to ensure that encryption is completely random.
  * They should be generated once per encryption.
  * You can store and display nonces -- they are not confidential but you must never reuse them.
  * @param raw byte array input
  */
class Nonce(val raw: Array[Byte]) extends AnyVal

object Nonce {

  private val random = new Random()

  /**
    * Creates a random nonce value.
    * @return Nonce
    */
  def createNonce(): Nonce = {
    import org.abstractj.kalium.NaCl.Sodium.CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES
    new Nonce(random.randomBytes(CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES))
  }

  /**
    * Reconstitute a nonce that has been stored with a ciphertext.
    * @param data byte array input
    * @return Nonce
    */
  def nonceFromBytes(data: Array[Byte]): Nonce = {
    import org.abstractj.kalium.NaCl.Sodium.CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES
    if (data == null || data.length != CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES) {
      throw new IllegalArgumentException("This nonce has an invalid size: " + data.length)
    }
    new Nonce(data)
  }

}
