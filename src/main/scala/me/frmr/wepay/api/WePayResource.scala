package me.frmr.wepay.api {
  import me.frmr.wepay._

  import net.liftweb.common._
  import net.liftweb.json._
    import JsonDSL._
    import Extraction._

  ////
  // Base resource and accompanying meta
  ////
  trait WePayResource[MyType <: WePayResource[MyType]] {
    self: MyType =>

    def meta: WePayResourceMeta[MyType]
  }

  trait WePayResourceMeta[Model <: WePayResource[Model]] {
    implicit val formats = DefaultFormats

    val className = this.getClass.getName.split("\\.").last.dropRight(1)

    def resource : String = className.toLowerCase
    def resourceIdentifier : String = resource + "_id"
    def extract(json:JValue) : Model
    def extractFindResults(json:JValue) : List[Model]

    def query(action:Option[String], requestBody:JValue)(implicit authorizationToken:Option[WePayToken] = None) = {
      for {
        resultingJson <- WePay.executeAction(authorizationToken, resource, action, requestBody)
      } yield {
        extract(resultingJson)
      }
    }

    def findQuery(searchParameters:JValue)(implicit authorizationToken:Option[WePayToken] = None) = {
      for {
        resultingJson <- WePay.executeAction(authorizationToken, resource, Some("find"), searchParameters)
      } yield {
        extractFindResults(resultingJson)
      }
    }

    def find(id:Long)(implicit authorizationToken:Option[WePayToken]) = query(None, (resourceIdentifier -> id))
  }

  ////
  // Immutable Resource and accompanying meta
  ////
  trait ImmutableWePayResource[MyType <: ImmutableWePayResource[MyType, CrudResponseType], CrudResponseType] extends WePayResource[MyType] {
    self: MyType =>

    def meta: ImmutableWePayResourceMeta[MyType, CrudResponseType]
    def _id : Option[Long]

    def save(implicit authorizationToken:Option[WePayToken]) = meta.save(this)
  }

  trait ImmutableWePayResourceMeta[Model <: ImmutableWePayResource[Model, CrudResponseType], CrudResponseType] extends WePayResourceMeta[Model] {
    def extractCrudResponse(json:JValue) : CrudResponseType

    def resultRetrievalQuery(action:Option[String], requestBody:JValue)(implicit authorizationToken:Option[WePayToken] = None) = {
      for {
        resultingJson <- WePay.executeAction(authorizationToken, resource, action, requestBody)
      } yield {
        extractCrudResponse(resultingJson)
      }
    }

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
