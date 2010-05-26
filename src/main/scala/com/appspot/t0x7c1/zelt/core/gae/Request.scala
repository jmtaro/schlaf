package com.appspot.t0x7c1.zelt.core.gae

import com.appspot.t0x7c1.zelt.core
import com.appspot.t0x7c1.zelt.core.gae
import javax.servlet.http.{HttpServletRequest}


class RequestHeader(req: HttpServletRequest) extends core.BaseRequestHeader{
  def path = req.getRequestURI
  def method = req.getMethod
}

class Request(req: HttpServletRequest) extends core.BaseRequest{
  type RequestHeader = gae.RequestHeader

  lazy val header = new gae.RequestHeader(req)

  lazy val parameter = new Parameter[String, String](req){
    def nextTuple(next: Entry) = next.getKey -> next.getValue()(0)
    def getTarget(key: String) = req.getParameter(key)
  }

  lazy val parameters = new Parameter[String, Array[String]](req){
    def nextTuple(next: Entry) = next.getKey -> next.getValue()
    def getTarget(key: String) = req.getParameterValues(key)
  }

}

private[gae] abstract class Parameter[A, B]
  (req: HttpServletRequest) extends collection.Map[A, B]{

  def get(key: A): Option[B] = Option(getTarget(key))

  def iterator = new collection.Iterator[(A, B)]{
    private type Map = java.util.Map[String, Array[String]]
    val underlying = req.getParameterMap.asInstanceOf[Map].entrySet.iterator
    def hasNext = underlying.hasNext
    def next: (A, B) = Parameter.this.nextTuple(underlying.next)
  }

  def + [B1 >: B](kv: (A, B1)) = collection.mutable.Map[A, B1]() ++ this + kv

  def - (key: A) = filter { case (k, v) => k != key }

  type Entry = java.util.Map.Entry[String, Array[String]]
  def nextTuple(entry: Entry): (A, B)
  def getTarget(key: A): B

}

