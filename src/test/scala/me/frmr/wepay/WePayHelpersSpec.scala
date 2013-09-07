package me.frmr.wepay

import org.scalatest._

import scala.concurrent._
  import duration._
import ExecutionContext.Implicits.global

import net.liftweb.common._
import net.liftweb.json._
  import JsonDSL._

import me.frmr.wepay.WePayHelpers._

class WePayHelpersSpec extends FunSpec {
  describe("The WePayHelpers Singleton") {
    it("should correctly unwrap Box[Future[Box[T]]] for Full[Future[Box[T]]]") {
      val theFull = Full("something awesoeme")
      val testFull = Full(Future(theFull))
      val unwrappedTestFull = unwrapBoxOfFuture(testFull)
      val valueOfFuture = Await.result(unwrappedTestFull, 1.minute)

      assert(valueOfFuture == theFull)
    }

    it("should correctly unwrap Box[Future[Box[T]]] for Failure") {
      val testFail = Failure("I have failed you sir.")
      val unwrappedTestFail = unwrapBoxOfFuture(testFail)
      val valueOfFuture = Await.result(unwrappedTestFail, 1.minute)

      assert(valueOfFuture == testFail)
    }

    it("should correctly camel case JValue fields") {
      val input =
        ("my_field_name" -> 2) ~
        ("my_sub_object" -> ("my_sub_field_name" -> "bacon"))

      val expected =
        ("myFieldName" -> 2) ~
        ("mySubObject" -> ("mySubFieldName" -> "bacon"))

      assert(camelCaseJsonFieldNames(input) == expected)
    }

    it("should correctly underscore JValue fields") {
      val input =
        ("myFieldName" -> 2) ~
        ("mySubObject" -> ("mySubFieldName" -> "bacon"))

      val expected =
        ("my_field_name" -> 2) ~
        ("my_sub_object" -> ("my_sub_field_name" -> "bacon"))

      println(underscoreJsonFieldNames(input))
      assert(underscoreJsonFieldNames(input) == expected)
    }
  }
}
