package me.frmr.wepay {
  import net.liftweb.util.Props
  import net.liftweb.common._

  object WePayTestHelpers {
    val testAuthorizationToken : Option[WePayToken] = {
      (Props.get("wepay.testAuthUserId"), Props.get("wepay.testAuthAccessToken")) match {
        case (Full(userId), Full(accessToken)) =>
          Some(WePayToken(userId.toLong, accessToken, "BEARER", None))
        case _ =>
          None
      }
    }

    val testAccountId = Props.get("wepay.testAccountId").openOr("0").toLong
  }
}
