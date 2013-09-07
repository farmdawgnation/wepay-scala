package me.frmr.wepay {
  import scala.concurrent.Future

  import net.liftweb.common._
  import net.liftweb.util._
    import Helpers._
  import net.liftweb.json._
    import Extraction._
    import JsonDSL._

  import WePayHelpers._

  import dispatch._, Defaults._

  import com.ning.http.client.Response

  /**
   * This class represents an error condition returned while
   * attempting to run an operation against WePay's API. When Failures
   * are returned as a result of a WePay operation, they will usually
   * contain a WePayError.
   *
   * @param error The error type.
   * @param errorDescription A friendlier, user readable error message.
  **/
  case class WePayError(error: String, errorDescription: String) {
    override def toString = error + ": " + errorDescription
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
  case class WePayToken(user_id: Long, access_token: String, token_type: String, expires_in: Option[String]) {
    /**
     * The HTTP header to be sent with requests using this token.
    **/
    val httpHeader = token_type.toLowerCase.capitalize + " " + access_token
  }

  case class WePayResponse(code: Int, json: JValue)

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
    private[wepay] val clientId = Props.get("wepay.clientId") ?~! "wepay.clientId property is required."
    private[wepay] val clientSecret = Props.get("wepay.clientSecret") ?~! "wepay.clientSecret property is required."

    // The OAuth Redirect URL (where people are sent after completeing OAuth)
    protected val oauthRedirectUrl = Props.get("wepay.oauthRedirectUrl") ?~! "wepay.oauthRedirectUrl property is required."

    // We've got to transmit an informative User-Agent :\
    protected val apiUserAgent = Props.get("wepay.userAgent") ?~! "wepay.userAgent property is required."

    // The endpoints we're talking to (without trailing slash)
    protected val apiEndpointBase = Props.get("wepay.apiEndpointBase") openOr "stage.wepayapi.com"
    protected val uiEndpointBase = Props.get("wepay.uiEndpointBase") openOr "stage.wepay.com"

    //The permissions we're going to be requesting from users who we authenticate
    //on WePay's system.
    protected val oauthPermissions = Props.get("wepay.oauthPermissions") ?~! "wepay.oauthPermissions permissions property is required."

    // API version
    protected val apiVersion = "v2"

    /**
     * This object transforms an incoming HTTP response from dispatch into
     * a WePay Response, which gives us the status code and raw JSON as a
     * Lift-JSON object.
    **/
    protected object AsWePayResponse extends (Response => WePayResponse) {
      def apply(response: Response) = {
        WePayResponse(
          response.getStatusCode(),
          camelCaseJsonFieldNames(as.lift.Json(response))
        )
      }
    }

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
        val oauth_url = host(uiEndpointBase) / apiVersion / "oauth2" / "authorize" <<?
          Map("client_id" -> clientId, "redirect_uri" -> oauthRedirectUrl, "scope" -> oauthPermissions)

        oauth_url.secure.toRequest.getRawUrl
      }
    }

    /**
     * Runs a query against the WePay API and passes the resulting JSON, if successful
     * to a handler that transforms the response into the appropreate type.
     *
     * @param request The Req object that should be used to make the request.
     * @param handler The function that will translate a Lift-JSON object to whatever type it should be.
     * @return A Full[T] on success. A ParamFailure in the event of an API error, and a Failure in the event of a Dispatch error.
    **/
    protected def responseForRequest[T](request: Req, handler: (JValue)=>T) = {
      // Run the query and then transform that into a WePay Response.
      val response = Http(request > AsWePayResponse).either

      response.collect {
        case Right(WePayResponse(200, json)) => Full(handler(json))

        case Right(WePayResponse(code, json)) =>
          val error =
            {
              tryo(json.extract[WePayError])
            } openOr {
              "WePay returned a " + code + " without valid JSON."
            }

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
    def retrieveToken(oauthCode: String) : Future[Box[WePayToken]] = {
      def doRequest(clientId: String, redirectUrl: String, clientSecret: String, defaultHeaders: Map[String, String]) = {
        val requestBody: String = compact(render(
          ("client_id" -> clientId) ~
          ("redirect_uri" -> redirectUrl) ~
          ("client_secret" -> clientSecret) ~
          ("code" -> oauthCode)
        ))

        val tokenRequest = (host(apiEndpointBase) / apiVersion / "oauth2" / "token" <:< defaultHeaders << requestBody).secure
        responseForRequest[WePayToken](tokenRequest, (json) => json.extract[WePayToken])
      }

      unwrapBoxOfFuture {
        for {
          clientId <- clientId
          oauthRedirectUrl <- oauthRedirectUrl
          clientSecret <- clientSecret
          defaultHeaders <- defaultHeaders
        } yield {
          doRequest(clientId, oauthRedirectUrl, clientSecret, defaultHeaders)
        }
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
     * @param requestJson The request JSON to be transmitted in the body of the post. Defaults to JObject(Nil), which represents an empty request body.
    **/
    def executeAction(accessToken: Option[WePayToken], module: String, action: Option[String], requestJson: JValue = JObject(Nil)): Future[Box[JValue]] = {
      def doRequest(defaultHeaders: Map[String, String]) = {
        val requestTarget = action.toList.foldLeft(host(apiEndpointBase) / apiVersion / module)(_ / _).secure
        val requestBody = compact(render(requestJson))
        val headers = accessToken.map { token =>
          Map("Authorization" -> token.httpHeader)
        }.toList.foldLeft(defaultHeaders)(_ ++ _)

        val request = {
          underscoreJsonFieldNames(requestJson) match {
            case JObject(Nil) =>
              requestTarget <:< headers
            case _ =>
              requestTarget <:< headers << requestBody
          }
        }
        responseForRequest[JValue](request, (json) => json)
      }

      unwrapBoxOfFuture {
        defaultHeaders.map { defaultHeaders =>
          doRequest(defaultHeaders)
        }
      }
    }
  }
}
