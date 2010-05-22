package com.appspot.t0x7c1.schlaf.phase.sample

import com.appspot.t0x7c1.schlaf.apps

class HelloWorld extends apps.Phase{
  override def execute {
    writer >> "hello world !"
  }
}

