package com.gu.ftp

import scalaz.Coproduct

package object algebra {

  trait :+:[F[_], G[_]] {
    type λ[α] = Coproduct[F, G, α]
  }

  type Alg0[A] = Coproduct[ConnectionAlgebra, CommandAlgebra, A]
  type Alg[A] = Coproduct[ReceiveAlgebra, Alg0, A]

  object all
    extends ConnectionInstances with ConnectionFunctions
    with CommandInstances with CommandFunctions
    with ReceiveInstances with ReceiveFunctions

}
