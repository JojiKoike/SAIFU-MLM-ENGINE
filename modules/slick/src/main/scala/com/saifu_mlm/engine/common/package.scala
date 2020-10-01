package com.saifu_mlm.engine

import java.util.UUID

package object common {

  val ERROR_CODE = -1

  val string2UUID: String => UUID = (id: String) => UUID.fromString(id)

}
