package com.appspot.t0x7c1.schlaf.apps

import com.appspot.t0x7c1.schlaf.apps
import com.appspot.t0x7c1.zelt.core
import com.appspot.t0x7c1.zelt.core.gae
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

abstract class Phase(
  var context: apps.Context )
  extends core.BasePhase with gae.PhaseBuilder[Phase] with gae.Logger
{
  type Context = apps.Context
  def this() = this(null)
  def * [B <: Phase](implicit m: Manifest[B]): B = {
    val phase = m.erasure.newInstance.asInstanceOf[B]
    phase.context = context; phase
  }
  def route = context.route
  def writer = context.handler.writer
  def request = context.handler.request
}

class Context(
  val handler: apps.Handler,
  val route: apps.Route ) extends core.BaseContext
{
  type Handler = apps.Handler
  type Route = apps.Route
}

class Handler(
  val request: apps.Request,
  val writer: apps.Writer ) extends core.BaseHandler
{
  type Request = apps.Request
  type Writer = apps.Writer
}

class RequestHeader(req: HttpServletRequest) extends core.BaseRequestHeader
{
  lazy protected val header = new gae.RequestHeader(req)
  def path = header.path
  def method = header.method
}

class Request(req: HttpServletRequest) extends core.BaseRequest
{
  type RequestHeader = apps.RequestHeader
  lazy protected val request = new gae.Request(req)
  lazy val header = new apps.RequestHeader(req)
  lazy val parameter = request.parameter
  lazy val parameters = request.parameters
}

class Writer(res: HttpServletResponse) extends core.BaseWriter
{
  type Response = apps.Response
  lazy protected val writer = new gae.Writer(res)

  def print(response: Response) = writer print response
  def >> [A](content: A) = writer >> content
}

class Route(
  val parameter: collection.Map[String, String] ) extends core.BaseRoute
{
  def this() = this{ Map[String, String]() }
}

class ResponseHeader extends core.BaseResponseHeader with gae.ResponseHeader

class Response(
  val status: Int,
  val content: String ) extends core.BaseResponse with gae.Response
{
  type ResponseHeader = apps.ResponseHeader
  lazy val header = new apps.ResponseHeader
}

class Dispatcher(handler: Handler){
  def via(router: Router.type){
    val (phase, route) = router.resolve(handler.request.header.path)
    phase.context = new Context(handler, route)
    phase.execute
  }
}

