package com.appspot.t0x7c1.schlaf.apps

import com.appspot.t0x7c1.schlaf.{apps, phase => ph}
import com.appspot.t0x7c1.zelt.core
import com.appspot.t0x7c1.zelt.core.gae.exception
import com.appspot.t0x7c1.zelt.misc.PhaseSelector


object Router extends core.BaseRouter{
  type Route = apps.Route
  type Phase = apps.Phase

  def resolve(path: String) =
    selector.find(path) match{
      case Some((phase, param)) => phase -> new apps.Route(param)
      case None => new ph.error.NotFound -> new apps.Route
    }

  private def selector = new PhaseSelector[apps.Phase]{
    def mapping = {
      "/:user_id" :> {
        "/:year/:month/:day/:entry_id"
        "/api" :> {
          "/article.:mode/:entry_id"
        }
      }
      "/hello" >> new ph.sample.HelloWorld
    }
  }

}

