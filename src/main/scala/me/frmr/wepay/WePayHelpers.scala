package me.frmr.wepay

import scala.concurrent.Future

import net.liftweb.common._
import net.liftweb.json._
import net.liftweb.util.StringHelpers._

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

  def camelCaseJsonFieldNames(underscoredJson: JValue) = {
    underscoredJson.transform {
      case field @ JField(name, _) =>
        field.copy(name = camelifyMethod(name))
    }
  }

  def underscoreJsonFieldNames(camelifiedJson: JValue) = {
    camelifiedJson.transform {
      case field @ JField(name, _) =>
        field.copy(name = snakify(name.capitalize))
    }
  }
}
