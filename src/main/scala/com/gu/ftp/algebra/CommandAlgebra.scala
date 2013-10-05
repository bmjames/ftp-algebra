package com.gu.ftp
package algebra

import scalaz.{:<:, Functor, Free, Inject}, Free.Return, Inject.inject

sealed trait CommandAlgebra[A]

case class PWD[A](h: String => A) extends CommandAlgebra[A]

case class CWD[A](directory: String, h: Boolean => A) extends CommandAlgebra[A]

case class ListFiles[A](h: List[File] => A) extends CommandAlgebra[A]

case class File(name: String, isDirectory: Boolean, lastModified: Long)

trait CommandInstances {

  implicit val commandAlgebraInstance: Functor[CommandAlgebra] =
    new Functor[CommandAlgebra] {
      def map[A, B](a: CommandAlgebra[A])(f: A => B): CommandAlgebra[B] =
        a match {
          case PWD(h)    => PWD(h andThen f)
          case CWD(d, h) => CWD(d, h andThen f)
          case ListFiles(h) => ListFiles(h andThen f)
        }
    }

}

trait CommandFunctions {

  private type Inj[F[_]] = CommandAlgebra :<: F

  private def inj[F[_]: Functor : Inj, A](ga: CommandAlgebra[Free[F, A]]): Free[F, A] =
    inject[F, CommandAlgebra, A](ga)

  def pwd[F[_] : Functor : Inj]: Free[F, String] =
    inj(PWD(Return(_)))

  def cwd[F[_] : Functor : Inj](path: String): Free[F, Boolean] =
    inj(CWD(path, Return(_)))

  def list[F[_]: Functor : Inj]: Free[F, List[File]] =
    inj(ListFiles(Return(_)))

}

object CommandAlgebra extends CommandInstances with CommandFunctions
