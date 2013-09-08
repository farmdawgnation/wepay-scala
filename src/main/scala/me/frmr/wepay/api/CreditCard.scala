package me.frmr.wepay.api {
  import net.liftweb.common.Box
  import net.liftweb.json._
    import JsonDSL._
    import Extraction._

  import me.frmr.wepay._
    import WePayHelpers._

  /**
   * Models a response from the credit_card API endpoints that don't
   * return an instance of a Credit Card.
   *
   * @param creditCardId The ID of the CreditCard.
   * @param state The current state of the CreditCard
  **/
  case class CreditCardResponse(creditCardId:Long, state:String)

  /**
   * A case class representing a billing address associated with the
   * credit card.
   *
   * @param address1 The first line of the street address.
   * @param address2 The second line of the street address.
   * @param city The city of the billing address.
   * @param state The state of the billing address.
   * @param country The country of the billing address.
   * @param zip The ZIP code of the billing address.
  **/
  case class CreditCardAddress(address1:String, address2:Option[String] = None,
                               city:String, state:String, country:String, zip:String)

  /**
   * An instance of a Credit Card on WePay.
   *
   * This class is unique because a handful of the values that are required for saving the
   * credit card will not be returned when you retrieve it later. I've annotated those values
   * in the description below. As always the most official docs are
   * [[https://www.wepay.com/developer/reference/credit_card WePay's]].
   *
   * @param userName The name of the cardholder.
   * @param email The email of the cardholder.
   * @param ccNumber The Credit Card Number. (Not returned on retrieval.)
   * @param cvv The CVV number. (Not returned on retrieval.)
   * @param expirationMonth The month of CC expiration. (Not returned on retrieval.)
   * @param expirationYear The year of the CC expiration. (Not returned on retrieval.)
   * @param address The billing address. (Not returned on retrieval.)
   * @param creditCardId The ID of the Credit Card assigned by WePay.
   * @param creditCardName The name of the Credit Card assigned by WePay.
   * @param state The current state of the card.
   * @param referenceId The Reference ID of the account the card is assoicated with.
   * @define THIS CreditCard
  **/
  case class CreditCard(userName:String, email:String,
                        ccNumber:Option[String] = None,
                        cvv:Option[Int] = None,
                        expirationMonth:Option[Int] = None,
                        expirationYear:Option[Int] = None,
                        address:Option[CreditCardAddress] = None,
                        creditCardId:Option[Long] = None,
                        creditCardName:Option[String] = None,
                        state:Option[String] = None,
                        referenceId:Option[String] = None) extends ImmutableWePayResource[CreditCard, CreditCardResponse] {

    val meta = CreditCard
    val _id = creditCardId

    /**
     * Authorize a card for use sometime in the future. Use this if you're not going to
     * immediately run a checkout with the credit card.
    **/
    def authorize = {
      unwrapBoxOfFuture {
        for {
          creditCardId <- (creditCardId:Box[Long]) ?~! "You cant authorize a card without an ID."
        } yield {
          meta.authorize(creditCardId)
        }
      }
    }
  }

  /**
   * Meta object for manipulating CreditCard resources on WePay.
   *
   * @define INSTANCE CreditCard
   * @define CRUDRESPONSETYPE CreditCardResponse
  **/
  object CreditCard extends ImmutableWePayResourceMeta[CreditCard, CreditCardResponse] {
    protected def extract(json:JValue) = json.extract[CreditCard]
    protected def extractFindResults(json:JValue) = json.extract[List[CreditCard]]
    protected def extractCrudResponse(json:JValue) = json.extract[CreditCardResponse]

    /**
     * Authorize a card for use sometime in the future. Use this if you're not going to
     * immediately run a checkout with the credit card.
     *
     * @param creditCardId The ID of the credit card to authorize.
    **/
    def authorize(creditCardId:Long) = {
      unwrapBoxOfFuture {
        for {
          clientId <- WePay.clientId
          clientSecret <- WePay.clientSecret
        } yield {
          resultRetrievalQuery(Some("authorize"),
                      ("client_id" -> clientId) ~
                      ("clientSecret" -> clientSecret) ~
                      ("creditCardId" -> creditCardId))
        }
      }
    }

    override def create(instance:CreditCard)(implicit authorizationToken:Option[WePayToken]) = {
      val decomposedObject = decompose(instance) match {
        case obj:JObject => obj
        case _ => JObject(Nil)
      }

      unwrapBoxOfFuture {
        for {
          clientId <- WePay.clientId
          request = ("clientId" -> clientId) ~ decomposedObject
        } yield {
          resultRetrievalQuery(Some("create"), request)
        }
      }
    }
  }
}
