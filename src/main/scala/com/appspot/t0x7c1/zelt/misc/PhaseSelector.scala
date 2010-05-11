package com.appspot.t0x7c1.zelt.misc

import collection.mutable
import com.appspot.t0x7c1.zelt.core.BasePhase


abstract class PhaseSelector[A <: BasePhase] extends PathSelector[A]

abstract class PathSelector[A]{
  val parameter = mutable.Map[String, String]()

  type Param = mutable.Map[String, String]
  type Result = Option[(A, Param)]

  class ArrowAssoc(pattern: String){
    val keyreg = ":[^/]+".r
    def :> (block: => Unit) = {
      val tmp = prefix
      prefix += pattern
      keyreg.replaceAllIn(prefix, "([^/]+)").r.findPrefixOf(target) match{
        case Some(o) => block
        case None =>
      }
      prefix = tmp
    }
    def >> (obj: => A): Result = {
      val pat = prefix + pattern
      val exp = keyreg.replaceAllIn(pat, "([^/]+)") + "$"
      val m = exp.r.pattern.matcher(target)
      if (m.find){
        val keys = keyreg.findAllIn(pat)
        for(i <- 1 to m.groupCount()) {
          parameter += keys.next.substring(1) -> m.group(i)
        }
        found(obj, parameter)
      }
      else None
    }
  }
  implicit def str2ArrowAssoc(pattern: String): ArrowAssoc = new ArrowAssoc(pattern)

  private var target: String = ""
  private var prefix: String = ""
  private var found = { (obj: A, param: Param) => Some((obj, param)) }

  def find(path: String): Result = {
    parameter.clear
    target = path
    prefix = ""
    found = { (obj: A, param: Param) => return Some((obj, param)) }
    mapping
  }

  def mapping: Result

}

