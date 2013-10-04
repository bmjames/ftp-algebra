package com.gu.ftp

import scalaz.Coproduct

package object algebra {

  trait :+:[F[_], G[_]] {
    type λ[α] = Coproduct[F, G, α]
  }

  type Alg[A] = Coproduct[ConnectionAlgebra, CommandAlgebra, A]

  object all
    extends ConnectionInstances with ConnectionFunctions
    with CommandInstances with CommandFunctions

}
