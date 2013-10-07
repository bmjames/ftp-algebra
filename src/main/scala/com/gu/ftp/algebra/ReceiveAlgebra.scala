package com.gu.ftp.algebra

import scalaz.{Free, :<:, Functor}
import scalaz.Free.Return, scalaz.Inject.inject


sealed trait ReceiveAlgebra[A]

case class ListFiles[A](h: List[File] => A) extends ReceiveAlgebra[A]

case class RetrieveToFile[A](name: String, localFile: String, h: Unit => A) extends ReceiveAlgebra[A]


case class File(name: String, isDirectory: Boolean, lastModified: Long)


trait ReceiveInstances {
  implicit val receiveInstance: Functor[ReceiveAlgebra] = new Functor[ReceiveAlgebra] {
    def map[A, B](fa: ReceiveAlgebra[A])(f: (A) => B) = fa match {
      case ListFiles(h) => ListFiles(h andThen f)
      case RetrieveToFile(n, l, h) => RetrieveToFile(n, l, h andThen f)
    }
  }
}

trait ReceiveFunctions {

  private type Inj[F[_]] = ReceiveAlgebra :<: F

  private def inj[F[_]: Functor : Inj, A](ga: ReceiveAlgebra[Free[F, A]]): Free[F, A] =
    inject[F, ReceiveAlgebra, A](ga)

  def list[F[_]: Functor : Inj]: Free[F, List[File]] =
    inj(ListFiles(Return(_)))

  def retrieveToFile[F[_] : Functor : Inj](name: String, localFile: String): Free[F, Unit] =
    inj(RetrieveToFile(name, localFile, Return(_)))

}

object ReceiveAlgebra extends ReceiveInstances with ReceiveFunctions
