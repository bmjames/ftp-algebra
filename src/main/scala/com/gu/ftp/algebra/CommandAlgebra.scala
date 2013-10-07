package com.gu.ftp
package algebra

import scalaz.{:<:, Functor, Free, Inject}, Free.Return, Inject.inject

sealed trait CommandAlgebra[A]

case class PWD[A](h: String => A) extends CommandAlgebra[A]

case class CWD[A](directory: String, h: Boolean => A) extends CommandAlgebra[A]


trait CommandInstances {

  implicit val commandAlgebraInstance: Functor[CommandAlgebra] =
    new Functor[CommandAlgebra] {
      def map[A, B](a: CommandAlgebra[A])(f: A => B): CommandAlgebra[B] =
        a match {
          case PWD(h)    => PWD(h andThen f)
          case CWD(d, h) => CWD(d, h andThen f)
        }
    }

}

trait CommandFunctions {

  type InjCommand[F[_]] = CommandAlgebra :<: F

  private def inj[F[_]: Functor : InjCommand, A](ga: CommandAlgebra[Free[F, A]]): Free[F, A] =
    inject[F, CommandAlgebra, A](ga)

  def pwd[F[_] : Functor : InjCommand]: Free[F, String] =
    inj(PWD(Return(_)))

  def cwd[F[_] : Functor : InjCommand](path: String): Free[F, Boolean] =
    inj(CWD(path, Return(_)))

}

object CommandAlgebra extends CommandInstances with CommandFunctions
