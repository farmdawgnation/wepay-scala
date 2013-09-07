package me.frmr.wepay

import scala.concurrent.Future

import net.liftweb.common._

import dispatch.Defaults._

object WePayHelpers {
  def unwrapBoxOfFuture[T](boxedFuture: => Box[Future[Box[T]]]): Future[Box[T]] = {
    boxedFuture match {
      case Full(future) =>
        future

      case somethingElse: EmptyBox =>
        Future(somethingElse)
    }
  }
}
