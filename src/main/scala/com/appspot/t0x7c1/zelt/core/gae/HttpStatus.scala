package com.appspot.t0x7c1.zelt.core.gae

import javax.servlet.http.HttpServletResponse


class HttpStatus(val code: Int)

object HttpStatus{
  case object NOT_FOUND
    extends HttpStatus(HttpServletResponse.SC_NOT_FOUND)// 404

  case object METHOD_NOT_ALLOWED
    extends HttpStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED)// 405

  case object INTERNAL_SERVER_ERROR
    extends HttpStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)// 500
}

