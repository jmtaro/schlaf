package com.appspot.t0x7c1.zelt.core


abstract class BasePhase{
  type Context <: BaseContext
  var context: Context
  def execute: Unit = ()
}

abstract class BaseContext{
  type Handler <: BaseHandler
  type Route <: BaseRoute
  def handler: Handler
  def route: Route
}

abstract class BaseHandler{
  type Request <: BaseRequest
  type Writer <: BaseWriter
  def request: Request
  def writer: Writer
}

abstract class BaseRequestHeader{
  def path: String
  def method: String
}

abstract class BaseRequest{
  type RequestHeader <: BaseRequestHeader
  def header: RequestHeader
  def parameter: collection.Map[String, String]
  def parameters: collection.Map[String, Array[String]]
  def switch[A](key: String)(block: String => A) = block(parameter.getOrElse(key, ""))
}

abstract class BaseResponseHeader{
  def += (kv: (String, String))
  def apply(key: String): Option[String]
}

abstract class BaseResponse{
  type ResponseHeader <: BaseResponseHeader
  def status: Int
  def header: ResponseHeader
  def content: String
}

abstract class BaseWriter{
  type Response <: BaseResponse
  def print(response: Response)
}

abstract class BaseRoute{
  def parameter: collection.Map[String, String]
  def switch[A](key: String)(block: String => A) = block(parameter.getOrElse(key, ""))
}

abstract class BaseRouter{
  type Route <: BaseRoute
  type Phase <: BasePhase
  def resolve(path: String): (Phase, Route)
}

