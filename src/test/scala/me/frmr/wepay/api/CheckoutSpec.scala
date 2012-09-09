package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import net.liftweb.common._

  import me.frmr.wepay._
    import WePayTestHelpers._
  import me.frmr.wepay.api._

  class CheckoutSpec extends FunSpec {
    implicit val authorizationToken = testAuthorizationToken

    describe("A Checkout") {
      var testCheckoutId:Box[Long] = None

      it("should be createable") {
        val saveResponse = Checkout(testAccountId, "A test checkout.", "PERSONAL", 10).save

        assert(saveResponse match {
          case Full(CheckoutResponse(checkoutId, _, _)) =>
            testCheckoutId = Full(checkoutId)
            true
          case resp @ _ =>
            println(resp)
            false
        })
      }

      it("should be retrievable after creation") {
        val retrieval = testCheckoutId.flatMap { checkoutId =>
          Checkout.find(checkoutId)
        }

        assert(retrieval match {
          case Full(checkoutInstance:Checkout) =>
            true
          case _ =>
            false
        })
      }

      it("should not be mutable after creation") {
        val saveResponse = testCheckoutId.flatMap { checkoutId =>
          val testCheckout = Checkout.find(checkoutId)
          testCheckout.flatMap(_.save)
        }

        assert(saveResponse match {
          case Failure(_, _, _) => true
          case _ => false
        })
      }
    }
  }
}
