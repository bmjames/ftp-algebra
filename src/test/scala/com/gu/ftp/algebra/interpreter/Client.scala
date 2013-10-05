package com.gu.ftp.algebra.interpreter

import java.io.OutputStream
import org.apache.commons.net.ftp.{FTPFile, FTPClient}
import scalaz.effect.IO

final class Client {

  private val client: FTPClient = new FTPClient

  def connect(host: String, port: Int): IO[Unit] =
    IO { client.connect(host, port) }

  def login(user: String, password: String): IO[Boolean] =
    IO { client.login(user, password) }

  def pwd: IO[String] =
    IO { client.printWorkingDirectory }

  def cwd(path: String): IO[Boolean] =
    IO { client.changeWorkingDirectory(path) }

  def enterLocalPassiveMode: IO[Unit] =
    IO { client.enterLocalPassiveMode() }

  def listFiles: IO[List[FTPFile]] =
    IO { client.listFiles.toList }

  def retrieveFile(path: String)(output: OutputStream): IO[Boolean] =
    IO { client.retrieveFile(path, output) }

  def completePendingCommand: IO[Unit] =
    IO { client.completePendingCommand() }

  def quit: IO[Unit] =
    IO { client.quit() }
}
