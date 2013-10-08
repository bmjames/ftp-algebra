package com.gu.ftp.algebra
package interpreter

import scalaz.{Free, Functor, \/-, -\/}
import scalaz.effect.IO
import scalaz.syntax.monad._

/** Interpreter implemented using the Apache Commons Net client, wrapped in scalaz.IO
  */
sealed abstract class Interpreter[F[_] : Functor] {
  def runAlgebra[A](algebra: F[IO[A]], client: Client): IO[A]
}

trait InterpreterInstances {

  implicit val connectionAlgebraInterpreter: Interpreter[ConnectionAlgebra] =
    new Interpreter[ConnectionAlgebra] {
      def runAlgebra[A](algebra: ConnectionAlgebra[IO[A]], client: Client): IO[A] =
        algebra match {
          case Connect(host, port, h)   => client.connect(host, port) >>= h
          case LogIn(user, password, h) => client.login(user, password) >>= h
          case Quit(h) => client.quit >>= h
        }
    }

  implicit val commandAlgebraInterpreter: Interpreter[CommandAlgebra] =
    new Interpreter[CommandAlgebra] {
      def runAlgebra[A](algebra: CommandAlgebra[IO[A]], client: Client): IO[A] =
        algebra match {
          case PWD(h)       => client.pwd >>= h
          case CWD(path, h) => client.cwd(path) >>= h
          case Delete(path, h) => client.delete(path) >>= h
        }
    }

  implicit val receiveInterpreter: Interpreter[ReceiveAlgebra] =
    new Interpreter[ReceiveAlgebra] {
      def runAlgebra[A](algebra: ReceiveAlgebra[IO[A]], client: Client): IO[A] =
        algebra match {
          case ListFiles(h) =>
            val files = client.listFiles.map(fs =>
              fs.map(f => File(f.getName,
                f.isDirectory,
                f.getTimestamp.getTimeInMillis)))
            client.enterLocalPassiveMode >> files >>= h
          case RetrieveToFile(name, localFile, h) =>
            val action = for {
              _      <- client.enterLocalPassiveMode
              _      <- client.setBinaryFileType
              output <- IO(new java.io.FileOutputStream(localFile))
              _      <- client.retrieveFile(name, output)
              _      <- IO(output.close())
            } yield ()
            action >>= h
        }
    }

  implicit def coproductAlgebraInterpreter[F[_] : Interpreter : Functor, G[_] : Interpreter : Functor]: Interpreter[(F:+:G)#λ] =
    new Interpreter[(F:+:G)#λ] {
      def runAlgebra[A](algebra: (F:+:G)#λ[IO[A]], client: Client) =
        algebra.run match {
          case -\/(fa) => Interpreter[F].runAlgebra(fa, client)
          case \/-(fa) => Interpreter[G].runAlgebra(fa, client)
        }
    }

}

trait InterpreterFunctions {
  def run[F[_] : Functor : Interpreter, A](algebra: Free[F, A], client: Client): IO[A] =
    algebra.runM[IO](fa => Interpreter[F].runAlgebra(fa.map(IO(_)), client))
}

object Interpreter extends InterpreterInstances with InterpreterFunctions {
  def apply[F[_] : Interpreter] = implicitly[Interpreter[F]]
}
