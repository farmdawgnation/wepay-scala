package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import net.liftweb.common._

  import me.frmr.wepay._
    import WePayTestHelpers._
  import me.frmr.wepay.api._

  class CreditCardSpec extends FunSpec {
    implicit val authorizationToken = testAuthorizationToken

    describe("A Credit Card") {
      it("should be createable") {
        val theAddress = CreditCardAddress("75 5th St NW", None, "Atlanta", "GA", "US", "30308")
        val saveResponse = CreditCard("Burt Reynolds", "no-reply@google.com",
                                      Some("4003830171874018"), Some(1234),
                                      Some(10), Some(2012), Some(theAddress)).save

        assert(saveResponse match {
          case Full(CreditCardResponse(ccId, _)) =>
            true
          case _ =>
            false
        }, saveResponse)
      }
    }
  }
}
