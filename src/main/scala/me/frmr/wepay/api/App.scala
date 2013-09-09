package me.frmr.wepay.api {
  import scala.concurrent.Future

  import net.liftweb.json._
    import JsonDSL._
    import Extraction._
  import net.liftweb.common._

  import me.frmr.wepay._
    import WePayHelpers._

  import dispatch.Defaults._

  /**
   * Class describing instances of the App resource on WePay.
   *
   * This essentially identifies an instance of an application approved for API access on WePay
   * and allows you to pragmatically manipulate elements shuch at the theme of your pages and
   * analytics tracking codes.
   *
   * @param clientId The client ID of the application.
   * @param status The approval state of the application.
   * @param themeObject An object describing the current theme of the application.
   * @param gaqDomains A list of UA-XXXX code for analytics tracking.
  **/
  case class App(
    clientId: Long,
    status: String,
    themeObject: JObject,
    gaqDomains: List[String]
  ) extends WePayResource[App] {
    val meta = App

    /**
     * Save this instace of the App to WePay.
    **/
    def save = meta.save(this)
  }

  /**
   * The Meta object for accessing the App resource.
   *
   * @define RESOURCE App
   * @define INSTANCE App
  **/
  object App extends WePayResourceMeta[App] {
    protected def extract(json:JValue) = json.extract[App]
    protected def extractFindResults(json:JValue) = json.extract[List[App]]

    /**
     * Retrieve the app from WePay.
    **/
    def find = {
      implicit val authorizationToken = None //Not needed.

      unwrapBoxOfFuture {
        for {
          id <- WePay.clientId
          secret <- WePay.clientSecret
        } yield {
          query(None, (
            ("clientId" -> id) ~
            ("clientSecret" -> secret)
          ))
        }
      }
    }

    /**
     * This overrides the superclass's find method to always return a Failure. You cannot
     * currently retrieve *other* apps by their ID, so this is a noop.
    **/
    override def find(id:Long)(implicit authorizationToken:Option[WePayToken]) =
      Future(Failure("You cannot find this resource by ID."))

    /**
     * Save an instance of the App to WePay's server.
     *
     * @param instance The App instance to save.
    **/
    def save(instance:App) = {
      val decomposedObject = decompose(instance) match {
        case obj:JObject => obj
        case _ => JObject(Nil)
      }

      unwrapBoxOfFuture {
        for {
          secret <- WePay.clientSecret
        } yield {
          query(Some("modify"), (
            ("clientSecret" -> secret) ~
            decomposedObject
          ))
        }
      }
    }
  }
}
