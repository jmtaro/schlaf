package com.appspot.t0x7c1.zelt.core.gae

import com.appspot.t0x7c1.zelt.core
import com.appspot.t0x7c1.zelt.core.gae
import javax.servlet.http.{HttpServletResponse}

// add some test-comments

abstract trait PhaseBuilder[A <: core.BasePhase]{
  import reflect.Manifest
  def * [B <: A](implicit m: Manifest[B]): B
}

abstract trait Logger {
  import java.util.logging.Logger
  lazy val log = Logger getLogger getClass.getName
}

abstract trait ResponseHeader extends core.BaseResponseHeader{
  protected val headers = collection.mutable.Map[String, String]()
  def += (kv: (String, String)) = headers += kv
  def apply(key: String): Option[String] = headers.get(key)
  def foreach(block: ((String, String)) => Unit) { headers.foreach(block) }
}

abstract trait Response extends core.BaseResponse{
  type ResponseHeader <: gae.ResponseHeader
}

class Writer(res: HttpServletResponse) extends core.BaseWriter{
  type Response = gae.Response

  def print(response: Response) {
    res.setStatus(response.status)
    response.header foreach {
      case (key, value) => res.setHeader(key, value)
    }
    res.getWriter.println(response.content)
  }

  def >> [A](content: A) {
    res.getWriter.println(content)
  }

}

