package com.gu.ftp

import scalaz.Coproduct

package object algebra {

  type Alg[A] = Coproduct[ConnectionAlgebra, CommandAlgebra, A]

  object all
    extends ConnectionInstances with ConnectionFunctions
    with CommandInstances with CommandFunctions

}
