package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import net.liftweb.common._

  import me.frmr.wepay._
    import WePayTestHelpers._
  import me.frmr.wepay.api._

  class CreditCardSpec extends FunSpec {
    implicit val authorizationToken = testAuthorizationToken

    describe("A Credit Card") {
      var creditCardId : Long = 0l

      it("should be createable") {
        val theAddress = CreditCardAddress("75 5th St NW", None, "Atlanta", "GA", "US", "30308")
        val saveResponse = CreditCard("Burt Reynolds", "no-reply@google.com",
                                      Some("4003830171874018"), Some(1234),
                                      Some(10), Some(2012), Some(theAddress)).save

        assert(saveResponse match {
          case Full(CreditCardResponse(ccId, _)) =>
            creditCardId = ccId
            true
          case _ =>
            false
        }, saveResponse)
      }

      it("should be authorizeable") {
        val authorizeResult = CreditCard.authorize(creditCardId)

        assert(authorizeResult match {
          case Full(CreditCardResponse(_, _)) =>
            true
          case _ =>
            false
        }, authorizeResult)
      }

      it("should be able to authorize a checkout") {
        val authorization = CheckoutAuthorization(None, Some(creditCardId), Some("credit_card"))
        val checkoutResponse = Checkout(testAccountId, "Text CC Checkout", "PERSONAL", 1.0,
                                        authorization = Some(authorization)).save

        assert(checkoutResponse match {
          case Full(resp:CheckoutResponse) if resp.state == Some("authorized") =>
            Checkout.cancel(resp.checkout_id, "Just a unit test.")
            true
          case _ =>
            false
        }, checkoutResponse)
      }
    }
  }
}
