package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import net.liftweb.common._

  import me.frmr.wepay._
    import WePayTestHelpers._
  import me.frmr.wepay.api._

  class WithdrawalSpec extends FunSpec {
    implicit val authorizationToken = testAuthorizationToken

    describe("A Withdrawal") {
      var testWithdrawalId = 0l

      it("should be creatable") {
        val saveResponse = Withdrawal(testAccountId).save

        assert(saveResponse match {
          case Full(WithdrawalResponse(withdrawal_id, _)) =>
            testWithdrawalId = withdrawal_id
            true
          case resp @ _ =>
            false
        }, saveResponse)
      }

      it("should be retrievable after creation") {
        val retrieval = Withdrawal.find(testWithdrawalId)

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
