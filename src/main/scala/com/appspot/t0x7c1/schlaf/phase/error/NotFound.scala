package com.appspot.t0x7c1.schlaf.phase.error

import com.appspot.t0x7c1.schlaf.apps
import com.appspot.t0x7c1.zelt.core.gae.HttpStatus.NOT_FOUND
import com.appspot.t0x7c1.zelt.core.gae


class NotFound extends apps.Phase{
  override def execute {
    val res = new apps.Response(
      status = NOT_FOUND.code,
      content = "not found"
    )
    res.header += "hoge" -> "fuga"
    writer print res
  }
}

