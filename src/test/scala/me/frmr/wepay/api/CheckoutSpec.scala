package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import net.liftweb.common._
  import net.liftweb.util.Props

  import me.frmr.wepay._
  import me.frmr.wepay.api._

  class CheckoutSpec extends FunSpec {
    implicit val authorizationToken : Option[WePayToken] =
      (Props.get("wepay.testAuthUserId"), Props.get("wepay.testAuthAccessToken")) match {
        case (Full(userId), Full(accessToken)) =>
          Some(WePayToken(userId.toLong, accessToken, "BEARER", None))
        case _ =>
          None
      }

    val testAccountId = Props.get("wepay.testAccountId").openOr("0").toLong

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
    }
  }
}
