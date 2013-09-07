package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import scala.concurrent._
    import duration._

  import language.postfixOps

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
        val saveResponse = Await.result(Account("Unit Account", "An account created by unit testing.").save, 1 minute)

        assert(saveResponse match {
          case Full(accountResp:AccountResponse) =>
            unitAccountId = accountResp.accountId getOrElse 0l
            true
          case _ =>
            false
        }, saveResponse)
      }

      it("should be retrievable after creation") {
        val retrieval = Await.result(Account.find(unitAccountId), 1 minute)
        unitAccountInstance = retrieval

        assert(retrieval match {
          case Full(accountInstance:Account) =>
            true
          case _ =>
            false
        }, retrieval)
      }

      it("should return a balance") {
        val balanceRetrieval = Await.result(Account.balance(unitAccountId), 1 minute)

        assert(balanceRetrieval match {
          case Full(accountResp:AccountResponse) if accountResp.availableBalance.isDefined =>
            true
          case _ =>
            false
        }, balanceRetrieval)
      }

      it("should be deleteable") {
        val deletionResult = unitAccountInstance.flatMap { account =>
          Await.result(account.delete, 1 minute)
        }

        assert(deletionResult match {
          case Full(accountResp:AccountResponse) if accountResp.accountId.isDefined =>
            true
          case _ =>
            false
        }, deletionResult)
      }
    }
  }
}
