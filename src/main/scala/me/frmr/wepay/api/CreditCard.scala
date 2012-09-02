package me.frmr.wepay.api {
  import net.liftweb.common.Box
  import net.liftweb.json._
    import JsonDSL._
    import Extraction._

  import me.frmr.wepay.WePayToken

  /**
   * Models a response from the credit_card API endpoints that don't
   * return an instance of a Credit Card.
   *
   * @param credit_card_id The ID of the CreditCard.
   * @param state The current state of the CreditCard
  **/
  case class CreditCardResponse(credit_card_id:Long, state:String)

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
   * @param user_name The name of the cardholder.
   * @param email The email of the cardholder.
   * @param cc_number The Credit Card Number. (Not returned on retrieval.)
   * @param cvv The CVV number. (Not returned on retrieval.)
   * @param expiration_month The month of CC expiration. (Not returned on retrieval.)
   * @param expiration_year The year of the CC expiration. (Not returned on retrieval.)
   * @param address The billing address. (Not returned on retrieval.)
   * @param credit_card_id The ID of the Credit Card assigned by WePay.
   * @param credit_card_name The name of the Credit Card assigned by WePay.
   * @param state The current state of the card.
   * @param reference_id The Reference ID of the account the card is assoicated with.
   * @define THIS CreditCard
  **/
  case class CreditCard(user_name:String, email:String,
                        cc_number:Option[String] = None,
                        cvv:Option[Int] = None,
                        expiration_month:Option[Int] = None,
                        expiration_year:Option[Int] = None,
                        address:Option[CreditCardAddress] = None,
                        credit_card_id:Option[Long] = None,
                        credit_card_name:Option[String] = None,
                        state:Option[String] = None,
                        reference_id:Option[String] = None) extends ImmutableWePayResource[CreditCard, CreditCardResponse] {

    val meta = CreditCard
    val _id = credit_card_id

    /**
     * Authorize a card for use sometime in the future. Use this if you're not going to
     * immediately run a checkout with the credit card.
    **/
    def authorize = {
      for {
        credit_card_id <- (credit_card_id:Box[Long]) ?~! "You cant authorize a card without an ID."
        result <- meta.authorize(credit_card_id)
      } yield {
        result
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
     * @param credit_card_id The ID of the credit card to authorize.
    **/
    def authorize(credit_card_id:Long) = {
      resultRetrievalQuery(Some("authorize"),
        ("client_id" -> "TODO") ~
        ("client_secret" -> "TODO") ~
        ("credit_card_id" -> credit_card_id))
    }

    override def create(instance:CreditCard)(implicit authorizationToken:Option[WePayToken]) = {
      val decomposedObject = decompose(instance) match {
        case obj:JObject => obj
        case _ => JObject(Nil)
      }

      val request = ("client_id" -> "TODO") ~ decomposedObject
      resultRetrievalQuery(Some("create"), request)
    }
  }
}
