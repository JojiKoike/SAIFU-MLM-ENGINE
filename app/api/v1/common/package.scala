package api.v1

package object common {

  // Constants Definitions
  val ERROR_CODE: Int = -1

  val SESSION_ID = "sessionId"

  val SESSION_DATA_COOKIE_NAME = "sessionData"

  // Utilities

  /**
    Remove Needless Character ("Some( "and  ")") from Decoded Cookie Value
    ex. Some(value) -> value
    */
  val someRemover: String => String = { (target: String) =>
    target.substring(5, target.length - 1)
  }

}
