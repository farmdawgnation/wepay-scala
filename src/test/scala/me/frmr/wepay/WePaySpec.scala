package me.frmr.mweay {
  import org.scalatest.FunSpec

  import net.liftweb.common._

  import me.frmr.wepay._

  class WePaySpec extends FunSpec {
    describe("The WePay Singleton") {
      it("should yield a valid authorization URL") {
        val authorizeUrl = WePay.authorizeUrl

        assert(authorizeUrl match {
          case Full(url) => true
          case _ => false
        })
      }
    }
  }
}
