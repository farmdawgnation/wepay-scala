package me.frmr.wepay {
  import net.liftweb.common._
  import net.liftweb.util._
  import net.liftweb.json._
    import Extraction._
    import JsonDSL._

  import dispatch._
    import liftjson.Js._
    import Http._

  case class WePayError(error:String, error_description:String) {
    override def toString = error + ": " + error_description
  }

  case class WePayToken(user_id:Long, access_token:String, token_type:String, expires_in:Option[String]) {
    val httpHeader = token_type.toLowerCase.capitalize + " " + access_token
  }

  case class WePayResponse(code:Int, json:JValue)

  object WePay extends WePayImpl
  trait WePayImpl {
    implicit val formats = DefaultFormats
    // Authorization credentials for the application itself.
    val clientId = Props.get("wepay.clientId") ?~! "wepay.clientId property is required."
    val clientSecret = Props.get("wepay.clientSecret") ?~! "wepay.clientSecret property is required."

    // The OAuth Redirect URL (where people are sent after completeing OAuth)
    val oauthRedirectUrl = Props.get("wepay.oauthRedirectUrl") ?~! "wepay.oauthRedirectUrl property is required."

    // We've got to transmit an informative User-Agent :\
    val apiUserAgent = Props.get("wepay.userAgent") ?~! "wepay.userAgent property is required."

    // The endpoints we're talking to (without trailing slash)
    val apiEndpointBase = :/(Props.get("wepay.apiEndpointBase") openOr "stage.wepayapi.com")
    val uiEndpointBase = :/(Props.get("wepay.uiEndpointBase") openOr "stage.wepay.com")

    //The permissions we're going to be requesting from users who we authenticate
    //on WePay's system.
    val oauthPermissions = Props.get("wepay.oauthPermissions") ?~! "wepay.oauthPermissions permissions property is required."

    // API version
    val apiVersion = "v2"

    // Default headers
    def defaultHeaders = {
      for {
        apiUserAgent <- apiUserAgent
      } yield {
        Map("User-Agent" -> apiUserAgent, "Content-Type" -> "application/json")
      }
    }

    // Generate the OAuth2 Authorize URL
    val authorizeUrl = {
      for {
        clientId <- clientId
        oauthRedirectUrl <- oauthRedirectUrl
        oauthPermissions <- oauthPermissions
      } yield {
        val oauth_url = uiEndpointBase / apiVersion / "oauth2" / "authorize" <<?
          Map("client_id" -> clientId, "redirect_uri" -> oauthRedirectUrl, "scope" -> oauthPermissions)

        (oauth_url.secure to_uri) toString
      }
    }

    // Handler for contacting the WePay server and getting a response
    // that we can parse.
    protected def responseForRequest[T](request:Request, handler:(JValue)=>T) = {
      // Our HTTP transport
      val http = new Http

      val codeHandler = Handler(request, (code, r, e) => code)

      val response =
        WePayResponse.tupled(http.x(request >+ { (request) =>
          (codeHandler, (request ># (json => json)))
        }))

      response.code match {
        case 200 => Full(handler(response.json))
        case _ =>
          val error = response.json.extract[WePayError]
          ParamFailure(error.toString, Empty, Empty, error)
      }
    }

    // Retrieve an OAuth token for a particular user based on the code
    // provided from the OAuth success
    def retrieveToken(oauthCode:String) : Box[WePayToken] = {
      def doRequest(clientId:String, redirectUrl:String, clientSecret:String, defaultHeaders:Map[String, String]) = {
        val requestBody : String = compact(render(
          ("client_id" -> clientId) ~
          ("redirect_uri" -> redirectUrl) ~
          ("client_secret" -> clientSecret) ~
          ("code" -> oauthCode)
        ))

        val tokenRequest = (uiEndpointBase / apiVersion / "oauth2" / "token" <:< defaultHeaders << requestBody).secure
        responseForRequest[WePayToken](tokenRequest, (json) => json.extract[WePayToken])
      }

      for {
        clientId <- clientId
        oauthRedirectUrl <- oauthRedirectUrl
        clientSecret <- clientSecret
        defaultHeaders <- defaultHeaders
        result <- doRequest(clientId, oauthRedirectUrl, clientSecret, defaultHeaders)
      } yield {
        result
      }
    }

    // Execute an action against the API.
    def executeAction(accessToken:Option[WePayToken], module:String, action:Option[String], requestJson:JValue) : Box[JValue] = {
      def doRequest(defaultHeaders:Map[String, String]) = {
        val requestTarget = action.toList.foldLeft(apiEndpointBase / apiVersion / module)(_ / _).secure
        val requestBody = compact(render(requestJson))
        val headers = accessToken.map { token =>
          Map("Authorization" -> token.httpHeader)
        }.toList.foldLeft(defaultHeaders)(_ ++ _)

        val request = requestTarget <:< headers << requestBody
        responseForRequest[JValue](request, (json) => json)
      }

      for {
        defaultHeaders <- defaultHeaders
        result <- doRequest(defaultHeaders)
      } yield {
        result
      }
    }
  }
}
