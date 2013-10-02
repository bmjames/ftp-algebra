package com.gu.ftp.algebra

import org.apache.commons.net.ftp.{FTPReply, FTPClient}
import scalaz._


sealed abstract class Interpreter[F[_] : Functor] {
  def runAlgebra[A](algebra: F[FTPClient => (A, FTPClient)], client: FTPClient): (A, FTPClient)
}

trait InterpreterInstances {

  object SuccessReply {
    def unapply(i: Int): Option[Int] =
      if (i >= 200 && i < 300) Some(i) else None
  }

  implicit val connectionAlgebraInterpreter: Interpreter[ConnectionAlgebra] =
    new Interpreter[ConnectionAlgebra] {
      def runAlgebra[A](algebra: ConnectionAlgebra[FTPClient => (A, FTPClient)], client: FTPClient) =
        algebra match {
          case User(username, h) =>
            val status = client.user(username) match {
              case FTPReply.NEED_PASSWORD  => PasswordRequired
              case FTPReply.USER_LOGGED_IN => Ok
              case _ => Error
            }
            h(status)(client)
          case Pass(password, h) =>
            val status = client.pass(password) match {
              case FTPReply.USER_LOGGED_IN => Ok
              case _ => Error
            }
            h(status)(client)
          case Quit(h) =>
            val status = client.quit() match {
              case SuccessReply(_) => Ok
              case _ => Error
            }
            h(status)(client)
        }
    }

  implicit val commandAlgebraInterpreter: Interpreter[CommandAlgebra] =
    new Interpreter[CommandAlgebra] {
      def runAlgebra[A](algebra: CommandAlgebra[FTPClient => (A, FTPClient)], client: FTPClient) =
        algebra match {
          case PWD(h) =>
            h(client.printWorkingDirectory)(client)
          case CWD(directory, h) =>
            val status = client.changeWorkingDirectory(directory) match {
              case true  => Ok
              case false => Error
            }
            h(status)(client)
        }
    }

  implicit def coproductAlgebraInterpreter[F[_] : Interpreter : Functor, G[_] : Interpreter : Functor]: Interpreter[({ type l[a] = Coproduct[F, G, a] })#l] = {
    type H[A] = Coproduct[F, G, A]
    new Interpreter[H] {
      def runAlgebra[A](algebra: H[FTPClient => (A, FTPClient)], client: FTPClient) =
        algebra.run match {
          case -\/(fa) => implicitly[Interpreter[F]].runAlgebra(fa, client)
          case \/-(fa) => implicitly[Interpreter[G]].runAlgebra(fa, client)
        }
    }
  }

}

trait InterpreterFunctions {
  def run[A](algebra: Free[F, A], client: FTPClient): A =
    algebra.resume.fold({ (a: F[Free[F, A]]) =>
      val (x, y) = implicitly[Interpreter[F]].runAlgebra(a.map(a => (c: FTPClient) => (a, c)), client)
      run(x, y)
    }, a => a)
}

object Interpreter extends InterpreterInstances with InterpreterFunctions
