package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import scala.concurrent._
    import duration._

  import language.postfixOps

  import net.liftweb.common._

  import me.frmr.wepay._
    import WePayTestHelpers._
  import me.frmr.wepay.api._

  class CheckoutSpec extends FunSpec {
    implicit val authorizationToken = testAuthorizationToken

    describe("A Checkout") {
      var testCheckoutId:Box[Long] = None

      it("should be createable") {
        val saveResponse = Await.result(Checkout(testAccountId, "A test checkout.", "PERSONAL", 10).save, 1 minute)

        assert(saveResponse match {
          case Full(CheckoutResponse(checkoutId, _, _)) =>
            testCheckoutId = Full(checkoutId)
            true
          case resp @ _ =>
            false
        }, saveResponse)
      }

      it("should be retrievable after creation") {
        val retrieval = testCheckoutId.flatMap { checkoutId =>
          Await.result(Checkout.find(checkoutId), 1 minute)
        }

        assert(retrieval match {
          case Full(checkoutInstance:Checkout) =>
            true
          case _ =>
            false
        }, retrieval)
      }

      it("should not be mutable after creation") {
        val saveResponse = testCheckoutId.flatMap { checkoutId =>
          val testCheckout = Await.result(Checkout.find(checkoutId), 1 minute)
          testCheckout.flatMap { checkout =>
            Await.result(checkout.save, 1 minute)
          }
        }

        assert(saveResponse match {
          case Failure(_, _, _) => true
          case _ => false
        }, saveResponse)
      }
    }
  }
}
