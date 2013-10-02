package com.gu.ftp.algebra

import scalaz.{Free, Inject, Functor}, Free.Return, Inject.inject

sealed trait ConnectionAlgebra[A]

final case class User[A](username: String, h: Status => A) extends ConnectionAlgebra[A]

final case class Pass[A](password: String, h: Status => A) extends ConnectionAlgebra[A]

final case class Quit[A](h: Status => A) extends ConnectionAlgebra[A]


trait ConnectionInstances {

  implicit val connectionAlgebraInstance: Functor[ConnectionAlgebra] =
    new Functor[ConnectionAlgebra] {
      def map[A, B](a: ConnectionAlgebra[A])(f: A => B): ConnectionAlgebra[B] =
        a match {
          case User(u, h) => User(u, h andThen f)
          case Pass(p, h) => Pass(p, h andThen f)
          case Quit(h)    => Quit(h andThen f)
        }
    }

}

trait ConnectionFunctions {

  private type Inj[F[_]] = Inject[ConnectionAlgebra, F]

  private def inj[F[_]: Functor : Inj, A](ga: ConnectionAlgebra[Free[F, A]]): Free[F, A] =
    inject[F, ConnectionAlgebra, A](ga)

  def user[F[_] : Functor : Inj](username: String): Free[F, Status] =
    inj(User(username, Return(_)))

  def pass[F[_] : Functor : Inj](password: String): Free[F, Status] =
    inj(Pass(password, Return(_)))

  def quit[F[_]: Functor : Inj]: Free[F, Status] =
    inj(Quit(Return(_)))

}

object ConnectionAlgebra extends ConnectionInstances with ConnectionFunctions
