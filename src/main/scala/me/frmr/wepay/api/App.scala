package me.frmr.wepay.api {
  import scala.concurrent.Future

  import net.liftweb.json._
    import JsonDSL._
    import Extraction._
  import net.liftweb.common._

  import me.frmr.wepay._

  import dispatch.Defaults._

  /**
   * Class describing instances of the App resource on WePay.
   *
   * This essentially identifies an instance of an application approved for API access on WePay
   * and allows you to pragmatically manipulate elements shuch at the theme of your pages and
   * analytics tracking codes.
   *
   * @param client_id The client ID of the application.
   * @param state The approval state of the application.
   * @param theme_object An object describing the current theme of the application.
   * @param gaq_domains A list of UA-XXXX code for analytics tracking.
  **/
  case class App(client_id:Long, state:String, theme_object:JObject, gaq_domains:List[String]) extends WePayResource[App] {
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

      val findResult = for {
        id <- WePay.clientId
        secret <- WePay.clientSecret
      } yield {
        query(None, (
          ("client_id" -> id) ~
          ("client_secret" -> secret)
        ))
      }

      findResult match {
        case Full(future) =>
          future

        case somethingElse: EmptyBox =>
          Future(somethingElse)
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

      val saveResult = for {
        secret <- WePay.clientSecret
      } yield {
        query(Some("modify"), (
          ("client_secret" -> secret) ~
          decomposedObject
        ))
      }

      saveResult match {
        case Full(future) =>
          future

        case somethingElse: EmptyBox =>
          Future(somethingElse)
      }
    }
  }
}
