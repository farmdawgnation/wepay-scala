package me.frmr.wepay {
  import org.scalatest._

  import net.liftweb.common._

  import me.frmr.wepay._

  class WePaySpec extends FunSpec {
    describe("The WePay Singleton") {
      it("should yield a valid authorization URL") {
        val authorizeUrl = WePay.authorizeUrl

        assert(authorizeUrl match {
          case Full(url) => true
          case somethingElse =>
            fail(somethingElse.toString)
            false
        })
      }
    }
  }
}
