package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import net.liftweb.common._

  import me.frmr.wepay._
    import WePayTestHelpers._
    import api._

  class AccountSpec extends FunSpec {
    implicit val authorizationToken = testAuthorizationToken

    describe("An Account") {
      var unitAccountId = 0l
      var unitAccountInstance : Box[Account] = Empty

      it("should be createable") {
        val saveResponse = Account("Unit Account", "An account created by unit testing.").save

        assert(saveResponse match {
          case Full(accountResp:AccountResponse) =>
            unitAccountId = accountResp.account_id getOrElse 0l
            true
          case _ =>
            false
        })
      }

      it("should be retrievable after creation") {
        val retrieval = Account.find(unitAccountId)
        unitAccountInstance = retrieval

        assert(retrieval match {
          case Full(accountInstance:Account) =>
            true
          case _ =>
            false
        })
      }

      it("should return a balance") {
        val balanceRetrieval = Account.balance(unitAccountId)

        assert(balanceRetrieval match {
          case Full(accountResp:AccountResponse) if accountResp.available_balance.isDefined =>
            true
          case _ =>
            false
        })
      }

      it("should be deleteable") {
        val deletionResult = unitAccountInstance.flatMap(_.delete)

        assert(deletionResult match {
          case Full(accountResp:AccountResponse) if accountResp.account_id.isDefined =>
            true
          case _ =>
            false
        })
      }
    }
  }
}
