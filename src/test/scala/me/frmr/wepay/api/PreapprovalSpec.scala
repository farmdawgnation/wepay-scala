package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import scala.concurrent._
    import duration._

  import language.postfixOps

  import net.liftweb.common._

  import me.frmr.wepay._
    import WePayTestHelpers._
    import api._

  class PreapprovalSpec extends FunSpec {
    implicit val authorizationToken = testAuthorizationToken

    describe("A Preapproval") {
      var unitPreapprovalId = 0l

      it("should be creatable") {
        val saveResponse = Await.result(Preapproval(testAccountId, 10, "Unit Test Preapproval", "once").save, 1 minute)

        assert(saveResponse match {
          case Full(preapprovalResp: PreapprovalResponse) =>
            unitPreapprovalId = preapprovalResp.preapprovalId
            true
          case _ =>
            false
        }, saveResponse)
      }

      it("should be retrievable after creation") {
        val retrieval = Await.result(Preapproval.find(unitPreapprovalId), 1 minute)

        assert(retrieval match {
          case Full(preapprovalInstance: Preapproval) =>
            true
          case _ =>
            false
        }, retrieval)
      }

      it("should be cancelable") {
        val cancelResponse = Await.result(Preapproval.cancel(unitPreapprovalId), 1 minute)

        assert(cancelResponse match {
          case Full(preapprovalResp: PreapprovalResponse) =>
            true
          case _ =>
            false
        }, cancelResponse)
      }
    }
  }
}
