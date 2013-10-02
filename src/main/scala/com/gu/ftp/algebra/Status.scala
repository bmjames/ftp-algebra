package com.gu.ftp.algebra

sealed trait Status

case object Ok extends Status

case object Error extends Status

case object PasswordRequired extends Status
