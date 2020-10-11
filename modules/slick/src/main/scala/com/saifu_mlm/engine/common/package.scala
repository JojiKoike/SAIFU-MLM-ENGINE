package com.saifu_mlm.engine

import java.util.UUID

package object common {

  val UPDATE_SUCCESS_CODE: Int = 1

  val ERROR_CODE: Int = -1

  val string2UUID: String => UUID = (id: String) => UUID.fromString(id)

}
