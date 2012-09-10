package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import net.liftweb.common._

  import me.frmr.wepay._
    import WePayTestHelpers._
    import api._

  class PreapprovalSpec extends FunSpec {
    implicit val authorizationToken = testAuthorizationToken

    describe("A Preapproval") {
      var unitPreapprovalId = 0l

      it("should be creatable") {
        val saveResponse = Preapproval(testAccountId, 10, "Unit Test Preapproval", "once").save

        assert(saveResponse match {
          case Full(preapprovalResp:PreapprovalResponse) =>
            unitPreapprovalId = preapprovalResp.preapproval_id
            true
          case _ =>
            false
        })
      }

      it("should be retrievable after creation") {
        val retrieval = Preapproval.find(unitPreapprovalId)

        assert(retrieval match {
          case Full(preapprovalInstance:Preapproval) =>
            true
          case _ =>
            false
        })
      }

      it("should be cancelable") {
        val cancelResponse = Preapproval.cancel(unitPreapprovalId)

        assert(cancelResponse match {
          case Full(preapprovalResp:PreapprovalResponse) =>
            true
          case _ =>
            false
        })
      }
    }
  }
}
