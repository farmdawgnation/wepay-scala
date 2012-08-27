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
   * retreival or manipulation of instances of WePay resources.
   *
   * @define RESOURCE the child class
   * @define INSTANCE WePayResource
  **/
  trait WePayResourceMeta[Model <: WePayResource[Model]] {
    implicit val formats = DefaultFormats

    val className = this.getClass.getName.split("\\.").last.dropRight(1)

    def resource : String = className.toLowerCase
    def resourceIdentifier : String = resource + "_id"
    def extract(json:JValue) : Model
    def extractFindResults(json:JValue) : List[Model]

    /**
     * Run a query against the $RESOURCE resource using a JValue that will result in a Box[JValue].
     *
     * @param action The action on $RESOURCE to request.
     * @param requestBody The JValue object representing the request payload.
    **/
    def query(action:Option[String], requestBody:JValue)(implicit authorizationToken:Option[WePayToken] = None) = {
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
    def findQuery(searchParameters:JValue)(implicit authorizationToken:Option[WePayToken] = None) = {
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
   * Children of this trait are instances of resources that cannot be modified by the application after their
   * creation on the server.
   *
   * @define THIS ImmutableWePayResource
  **/
  trait ImmutableWePayResource[MyType <: ImmutableWePayResource[MyType, CrudResponseType], CrudResponseType] extends WePayResource[MyType] {
    self: MyType =>

    def meta: ImmutableWePayResourceMeta[MyType, CrudResponseType]
    def _id : Option[Long]

    /**
     * Save an instance of the $THIS to WePay's server. If the $THIS already exists, this will
     * result in a Failure.
    **/
    def save(implicit authorizationToken:Option[WePayToken]) = meta.save(this)
  }

  /**
   * Children of this trait are meta objects used to find or save resource instances that cannot be modified
   * after their creation.
   *
   * @define INSTANCE ImmutableWePayResource
   * @define CRUDRESPONSETYPE CrudResponseType
  **/
  trait ImmutableWePayResourceMeta[Model <: ImmutableWePayResource[Model, CrudResponseType], CrudResponseType] extends WePayResourceMeta[Model] {
    def extractCrudResponse(json:JValue) : CrudResponseType

    /**
     * Run some query against WePay that will result in a a Full Box containing $CRUDRESPONSETYPE on success, or a
     * Failure on error.
    **/
    def resultRetrievalQuery(action:Option[String], requestBody:JValue)(implicit authorizationToken:Option[WePayToken] = None) = {
      for {
        resultingJson <- WePay.executeAction(authorizationToken, resource, action, requestBody)
      } yield {
        extractCrudResponse(resultingJson)
      }
    }

    /**
     * Save an instance of $RESOURCE to the WePay server.
     *
     * You probably shouldn't call this directly. Call on $RESOURCE.save instead, for good form.
     *
     * @param instance The $RESOURCE to save.
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

  ////
  // Mutable Resource and accompanying meta
  ////
  trait MutableWePayResource[MyType <: MutableWePayResource[MyType, CrudResponseType], CrudResponseType] extends ImmutableWePayResource[MyType, CrudResponseType] {
    self: MyType =>

    def meta : MutableWePayResourceMeta[MyType, CrudResponseType]

    def delete(implicit authorizationToken:Option[WePayToken]) = meta.delete(this)
  }

  trait MutableWePayResourceMeta[Model <: MutableWePayResource[Model, CrudResponseType], CrudResponseType] extends ImmutableWePayResourceMeta[Model, CrudResponseType] {
    override def save(instance:Model)(implicit authorizationToken:Option[WePayToken]) = {
      instance._id match {
        case Some(_) => modify(instance)
        case _ => create(instance)
      }
    }

    protected def modify(instance:Model)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("modify"), decompose(instance))
    }

    def delete(instance:Model)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("delete"), (resourceIdentifier -> instance._id))
    }
  }
}
