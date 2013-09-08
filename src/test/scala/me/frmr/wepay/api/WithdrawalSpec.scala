package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import scala.concurrent._
    import duration._

  import language.postfixOps

  import net.liftweb.common._

  import me.frmr.wepay._
    import WePayTestHelpers._
  import me.frmr.wepay.api._

  class WithdrawalSpec extends FunSpec {
    implicit val authorizationToken = testAuthorizationToken

    describe("A Withdrawal") {
      var testWithdrawalId = 0l

      it("should be creatable") {
        val saveResponse = Await.result(Withdrawal(testAccountId).save, 1 minute)

        assert(saveResponse match {
          case Full(WithdrawalResponse(withdrawalId, _)) =>
            testWithdrawalId = withdrawalId
            true
          case resp @ _ =>
            false
        }, saveResponse)
      }

      it("should be retrievable after creation") {
        val retrieval = Await.result(Withdrawal.find(testWithdrawalId), 1 minute)

        assert(retrieval match {
          case Full(_:Withdrawal) =>
            true
          case _ =>
            false
        }, retrieval)
      }
    }
  }
}
