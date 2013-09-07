package me.frmr.wepay.api {
  import org.scalatest.FunSpec

  import scala.concurrent._
    import duration._

  import language.postfixOps

  import net.liftweb.common._

  import me.frmr.wepay._
    import WePayTestHelpers._
  import me.frmr.wepay.api._

  class UserSpec extends FunSpec {
    implicit val authorizationToken = testAuthorizationToken

    describe("A User") {
      it("should be retrievable") {
        val retrieval = Await.result(User(), 1 minute)

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
