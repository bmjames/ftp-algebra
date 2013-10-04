package com.gu.ftp.algebra

import scalaz.{:<:, Free, Inject, Functor}, Free.Return, Inject.inject

sealed trait ConnectionAlgebra[A]

final case class Connect[A](host: String, port: Int, h: Unit => A) extends ConnectionAlgebra[A]

final case class LogIn[A](user: String, password: String, h: Boolean => A) extends ConnectionAlgebra[A]

final case class Quit[A](h: Unit => A) extends ConnectionAlgebra[A]


trait ConnectionInstances {

  implicit val connectionAlgebraInstance: Functor[ConnectionAlgebra] =
    new Functor[ConnectionAlgebra] {
      def map[A, B](a: ConnectionAlgebra[A])(f: A => B): ConnectionAlgebra[B] =
        a match {
          case Connect(host, p, h) => Connect(host, p, h andThen f)
          case LogIn(u, p, h)      => LogIn(u, p, h andThen f)
          case Quit(h)             => Quit(h andThen f)
        }
    }

}

trait ConnectionFunctions {

  private type Inj[F[_]] = ConnectionAlgebra :<: F

  private def inj[F[_]: Functor : Inj, A](ga: ConnectionAlgebra[Free[F, A]]): Free[F, A] =
    inject[F, ConnectionAlgebra, A](ga)

  def connect[F[_] : Functor : Inj](host: String, port: Int = 21): Free[F, Unit] =
    inj(Connect(host, port, Return(_)))

  def login[F[_] : Functor : Inj](user: String, password: String): Free[F, Boolean] =
    inj(LogIn(user, password, Return(_)))

  def quit[F[_]: Functor : Inj]: Free[F, Unit] =
    inj(Quit(Return(_)))

}

object ConnectionAlgebra extends ConnectionInstances with ConnectionFunctions
