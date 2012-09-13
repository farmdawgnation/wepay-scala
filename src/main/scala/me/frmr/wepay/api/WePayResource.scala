package me.frmr.wepay.api {
  import me.frmr.wepay._

  import net.liftweb.common._
  import net.liftweb.json._
    import JsonDSL._
    import Extraction._

  /**
   * The base definition for any instance of an object stored on WePay.
   *
   * This definition is the bare-bones least common denominator between all
   * things stored on WePay and provides no predefined instance functionality.
  **/
  trait WePayResource[MyType <: WePayResource[MyType]] {
    self: MyType =>

    def meta: WePayResourceMeta[MyType]
  }

  /**
   * The Meta trait paired with the base WePayResource.
   *
   * This Meta trait should be used in Meta Objects that are used in the
   * retreival or manipulation of instances of WePay resources. This trait
   * provides functionality for a meta object that is able to run a search based
   * on some search parameters provided in the form of a lift-json object, run some
   * arbitrary query, or retrieve an instance directly by its ID.
   *
   * @define RESOURCE the child class
   * @define INSTANCE WePayResource
  **/
  trait WePayResourceMeta[Model <: WePayResource[Model]] {
    implicit val formats = DefaultFormats

    protected val className = this.getClass.getName.split("\\.").last.dropRight(1)

    protected def resource : String = className.toLowerCase
    protected def resourceIdentifier : String = resource + "_id"
    protected def extract(json:JValue) : Model
    protected def extractFindResults(json:JValue) : List[Model]

    /**
     * Run a query against the $RESOURCE resource using a JValue that will result in a Box[JValue].
     *
     * @param action The action on $RESOURCE to request.
     * @param requestBody The JValue object representing the request payload.
    **/
    protected def query(action:Option[String], requestBody:JValue = JObject(Nil))(implicit authorizationToken:Option[WePayToken] = None) = {
      for {
        resultingJson <- WePay.executeAction(authorizationToken, resource, action, requestBody)
      } yield {
        extract(resultingJson)
      }
    }

    /**
     * Run a search for all $INSTANCE matching the searchParameters.
     *
     * @param searchParameters The lift-json JValue representing the search parameters.
    **/
    protected def findQuery(searchParameters:JValue)(implicit authorizationToken:Option[WePayToken] = None) = {
      for {
        resultingJson <- WePay.executeAction(authorizationToken, resource, Some("find"), searchParameters)
      } yield {
        extractFindResults(resultingJson)
      }
    }

    /**
     * Find a $INSTANCE in WePay's system by ID.
    **/
    def find(id:Long)(implicit authorizationToken:Option[WePayToken]) = query(None, (resourceIdentifier -> id))
  }

  /**
   * This is the parent class of WePayResources that can be saved to the server, but cannot be modified after
   * their initial save.
   *
   * This class of resources included Checkout, Preapproval, and Withdrawal. These objects cannot be changed via
   * the API and are deemed ImmutableWePayResources for the purposes of this API. An additional property of these
   * resources is the addition of the `CrudResponseType`, which is an additional type of response you can receive
   * from the server. While many operations result in the delivery of an instance, some result in the delivery
   * of some other, smaller object containing basic information you need to finish a workflow.
   *
   * @define THIS ImmutableWePayResource
   * @define SAVEBEHAVIOR If the $THIS already exists, this will result in a Failure.
  **/
  trait ImmutableWePayResource[MyType <: ImmutableWePayResource[MyType, CrudResponseType], CrudResponseType] extends WePayResource[MyType] {
    self: MyType =>

    def meta: ImmutableWePayResourceMeta[MyType, CrudResponseType]
    def _id : Option[Long]

    /**
     * Save an instance of the $THIS to WePay's server. $SAVEBEHAVIOR
    **/
    def save(implicit authorizationToken:Option[WePayToken]) = meta.save(this)
  }

  /**
   * Children of this trait are meta objects used to find or save resource instances that cannot be modified
   * after their creation.
   *
   * This class of resources include Checkout, Preapproval, and Withdrawal. Additionally, this Meta object
   * also introduced the CrudResponseType for operations such as save that return JSON of a different type
   * than those that return an instance of the object.
   *
   * @define INSTANCE ImmutableWePayResource
   * @define CRUDRESPONSETYPE CrudResponseType
  **/
  trait ImmutableWePayResourceMeta[Model <: ImmutableWePayResource[Model, CrudResponseType], CrudResponseType] extends WePayResourceMeta[Model] {
    protected def extractCrudResponse(json:JValue) : CrudResponseType

    /**
     * Run some query against WePay that will result in a a Full Box containing $CRUDRESPONSETYPE on success, or a
     * Failure on error.
    **/
    protected def resultRetrievalQuery(action:Option[String], requestBody:JValue)(implicit authorizationToken:Option[WePayToken] = None) = {
      for {
        resultingJson <- WePay.executeAction(authorizationToken, resource, action, requestBody)
      } yield {
        extractCrudResponse(resultingJson)
      }
    }

    /**
     * Save an instance of $INSTANCE to the WePay server.
     *
     * You probably shouldn't call this directly. Call on $RESOURCE.save instead, for good form.
     *
     * @param instance The $INSTANCE to save.
    **/
    def save(instance:Model)(implicit authorizationToken:Option[WePayToken]) : Box[CrudResponseType] = {
      instance._id match {
        case Some(_) => Failure("You can't update an immutable resource.")
        case _ => create(instance)
      }
    }

    protected def create(instance:Model)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("create"), decompose(instance))
    }
  }

  /**
   * This trait defines those resources that can be altered and deleted after creation.
   *
   * Currently, this just includes the Account resource.
   *
   * @define THIS MutableWePayResource
   * @define SAVEBEHAVIOR If the $THIS already exists, this will update the existing object.
  **/
  trait MutableWePayResource[MyType <: MutableWePayResource[MyType, CrudResponseType], CrudResponseType] extends ImmutableWePayResource[MyType, CrudResponseType] {
    self: MyType =>

    def meta : MutableWePayResourceMeta[MyType, CrudResponseType]

    /**
     * Delete the $THIS object this class represents.
    **/
    def delete(implicit authorizationToken:Option[WePayToken]) = meta.delete(this)
  }

  /**
   * This trait defined the meta behavior for MutableWePayResources, and can be used to find and manipulate them.
   *
   * Currently, this just includes the Account resource, but others may appear in the future.
   *
   * @define INSTANCE MutableWePayResource
   * @define CRUDRESPONSETYPE CrudResponseType
  **/
  trait MutableWePayResourceMeta[Model <: MutableWePayResource[Model, CrudResponseType], CrudResponseType] extends ImmutableWePayResourceMeta[Model, CrudResponseType] {
    /**
     * Create the $INSTANCE on the server if it does not exist. If it does exist, update it.
    **/
    override def save(instance:Model)(implicit authorizationToken:Option[WePayToken]) = {
      instance._id match {
        case Some(_) => modify(instance)
        case _ => create(instance)
      }
    }

    protected def modify(instance:Model)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("modify"), decompose(instance))
    }

    /**
     * Delete the $INSTANCE on the server.
    **/
    def delete(instance:Model)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("delete"), (resourceIdentifier -> instance._id))
    }
  }
}
