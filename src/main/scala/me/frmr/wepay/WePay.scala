package me.frmr.wepay {
  import net.liftweb.common._
  import net.liftweb.util._
  import net.liftweb.json._
    import Extraction._
    import JsonDSL._

  import dispatch._

  import com.ning.http.client.{Request, RequestBuilder, Response}

  /**
   * This class represents an error condition returned while
   * attempting to run an operation against WePay's API. When Failures
   * are returned as a result of a WePay operation, they will usually
   * contain a WePayError.
   *
   * @param error The error type.
   * @param error_description A friendlier, user readable error message.
  **/
  case class WePayError(error:String, error_description:String) {
    override def toString = error + ": " + error_description
  }

  /**
   * An access token that authenticates your application to
   * perform actions on behalf of a certain user on WePay's
   * servers.
   *
   * @param user_id The ID of the uer this token relates to on WePay.
   * @param access_token The actual token.
   * @param token_type The type of token. Currently, always "BEARER"
   * @param expires_in An expiration date associated with the token, if any.
  **/
  case class WePayToken(user_id:Long, access_token:String, token_type:String, expires_in:Option[String]) {
    /**
     * The HTTP header to be sent with requests using this token.
    **/
    val httpHeader = token_type.toLowerCase.capitalize + " " + access_token
  }

  case class WePayResponse(code:Int, json:JValue)

  /**
   * The Default WePay singleton with no additional functionality
   * over WePayImpl.
  **/
  object WePay extends WePayImpl

  /**
   * The base WePay implementation.
   *
   * This trait defines all the properties and functionality required
   * to interact with the WePay API.
  **/
  trait WePayImpl {
    implicit val formats = DefaultFormats
    // Authorization credentials for the application itself.
    protected val clientId = Props.get("wepay.clientId") ?~! "wepay.clientId property is required."
    protected val clientSecret = Props.get("wepay.clientSecret") ?~! "wepay.clientSecret property is required."

    // The OAuth Redirect URL (where people are sent after completeing OAuth)
    protected val oauthRedirectUrl = Props.get("wepay.oauthRedirectUrl") ?~! "wepay.oauthRedirectUrl property is required."

    // We've got to transmit an informative User-Agent :\
    protected val apiUserAgent = Props.get("wepay.userAgent") ?~! "wepay.userAgent property is required."

    // The endpoints we're talking to (without trailing slash)
    protected val apiEndpointBase = :/(Props.get("wepay.apiEndpointBase") openOr "stage.wepayapi.com")
    protected val uiEndpointBase = :/(Props.get("wepay.uiEndpointBase") openOr "stage.wepay.com")

    //The permissions we're going to be requesting from users who we authenticate
    //on WePay's system.
    protected val oauthPermissions = Props.get("wepay.oauthPermissions") ?~! "wepay.oauthPermissions permissions property is required."

    // API version
    protected val apiVersion = "v2"

    // Default headers
    protected def defaultHeaders = {
      for {
        apiUserAgent <- apiUserAgent
      } yield {
        Map("User-Agent" -> apiUserAgent, "Content-Type" -> "application/json")
      }
    }

    /**
     * The URL that you should redirect your users to so that they may begin the
     * OAuth workflow to authorize your application to act on their behalf.
    **/
    val authorizeUrl = {
      for {
        clientId <- clientId
        oauthRedirectUrl <- oauthRedirectUrl
        oauthPermissions <- oauthPermissions
      } yield {
        val oauth_url = uiEndpointBase / apiVersion / "oauth2" / "authorize" <<?
          Map("client_id" -> clientId, "redirect_uri" -> oauthRedirectUrl, "scope" -> oauthPermissions)

        oauth_url.secure.build.getRawUrl
      }
    }

    // Handler for contacting the WePay server and getting a response
    // that we can parse.
    protected def responseForRequest[T](request:RequestBuilder, handler:(JValue)=>T) = {
      object CodeAndWePayResponse extends (Response => WePayResponse) {
        def apply(r:Response) = {
          WePayResponse(
            r.getStatusCode(),
            as.lift.Json(r)
          )
        }
      }

      // Our HTTP transport
      val response = Http(request > CodeAndWePayResponse).either

      response() match {
        case Right(WePayResponse(200, json)) => Full(handler(json))
        case Right(WePayResponse(_, json)) =>
          val error = json.extract[WePayError]
          ParamFailure(error.toString, Empty, Empty, error)
        case Left(error) =>
          Failure("Error from dispatch: " + error)
      }
    }

    /**
     * Retrieve the WePayToken for a user based on a code passed into oauthRedirectUrl
     * by WePay.
     *
     * @param oauthCode The code passed in by WePay in the code GET parameter.
    **/
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

    /**
     * Executes some action against the WePay API.
     *
     * This is a raw request that will take in a lift-json JValue as the request and
     * produce a Box[JValue] as a response.
     *
     * @param accessToken The WePayToken associated with this request, if any.
     * @param module The module associated with this request. So if the URL you wanted to hit was /checkout/create, this would be "checkout".
     * @param action The action associated wiht this request. For "/checkout/create" it would be "create". Set to None for no action.
     * @param requestJson The request JSON to be transmitted in the body of the post, if any.
    **/
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
