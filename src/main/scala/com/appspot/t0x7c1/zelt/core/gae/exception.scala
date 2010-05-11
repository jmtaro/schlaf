package com.appspot.t0x7c1.zelt.core.gae.exception

import com.appspot.t0x7c1.zelt.core.gae.HttpStatus
import com.appspot.t0x7c1.zelt.core.gae.{HttpStatus => HS}

class HttpStatusException(val status: HttpStatus, msg: String)
  extends Exception(msg)
{
  def message = "(%s) [%s] %s" format(status.code, status, msg)
}

class NotFound(path: String)
  extends HttpStatusException(HS.NOT_FOUND, path)

class InternalServerError(msg: String)
  extends HttpStatusException(HS.INTERNAL_SERVER_ERROR, msg)

class MethodNotAllowed(msg: String)
  extends HttpStatusException(HS.METHOD_NOT_ALLOWED, msg)

