package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import scala.concurrent._
    import duration._

  import language.postfixOps

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
        val creditCard = CreditCard("Burt Reynolds", "no-reply@google.com",
                                      Some("4003830171874018"), Some(1234),
                                      Some(10), Some(2019), Some(theAddress))
        val saveResponse = Await.result(creditCard.save, 1 minute)

        assert(saveResponse match {
          case Full(CreditCardResponse(ccId, _)) =>
            creditCardId = ccId
            true
          case _ =>
            false
        }, saveResponse)
      }

      it("should be authorizeable") {
        val authorizeResult = Await.result(CreditCard.authorize(creditCardId), 1 minute)

        assert(authorizeResult match {
          case Full(CreditCardResponse(_, _)) =>
            true
          case _ =>
            false
        }, authorizeResult)
      }

      it("should be able to authorize a checkout") {
        val authorization = CheckoutAuthorization(None, Some(creditCardId), Some("credit_card"))
        val checkout = Checkout(testAccountId, "Test CC Checkout", "PERSONAL", 1.0,
                                authorization = Some(authorization))
        val checkoutResponse = Await.result(checkout.save, 1 minute)

        assert(checkoutResponse match {
          case Full(resp:CheckoutResponse) if resp.state == Some("authorized") =>
            Checkout.cancel(resp.checkoutId, "Just a unit test.")
            true
          case _ =>
            false
        }, checkoutResponse)
      }
    }
  }
}
