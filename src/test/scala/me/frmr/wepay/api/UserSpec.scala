package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import net.liftweb.common._

  import me.frmr.wepay._
    import WePayTestHelpers._
  import me.frmr.wepay.api._

  class UserSpec extends FunSpec {
    implicit val authorizationToken = testAuthorizationToken

    describe("A User") {
      it("should be retrievable") {
        val retrieval = User()

        assert(retrieval match {
          case Full(_:User) =>
            true
          case _ =>
            false
        }, retrieval)
      }
    }
  }
}
