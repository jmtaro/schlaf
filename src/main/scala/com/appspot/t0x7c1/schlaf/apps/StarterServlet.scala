package com.appspot.t0x7c1.schlaf.apps

import com.appspot.t0x7c1.zelt.core
import com.appspot.t0x7c1.zelt.core.gae
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}


class StarterServlet extends HttpServlet with gae.Logger
{
  override def service(req: HttpServletRequest, res: HttpServletResponse) = {
    req.setCharacterEncoding("UTF-8")
    res.setCharacterEncoding("UTF-8")

    val request = new Request(req)
    val handler = new Handler(request, new Writer(res))
    try{
      new Dispatcher(handler) via Router
    } catch {
      case e: Exception => log severe format(uncaught(e), request.header)
    }
  }

  def format(message: String, header: core.BaseRequestHeader) = {
    "[%s] %s %s".format(header.method, header.path, message)
  }

  def uncaught(e: Throwable) = {
    "uncaught exception: " +
      ( e.toString /: e.getStackTrace.toList.take(20) ){ _ + "\n\t" + _ }
  }

}

