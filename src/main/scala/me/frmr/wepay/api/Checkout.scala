package me.frmr.wepay.api {
  import net.liftweb.json._
    import JsonDSL._
    import Serialization._
  import net.liftweb.common.Box

  import me.frmr.wepay._

  /**
   * The response type for API operations that don't return an instance of Checkout, or some variation
   * thereof.
   *
   * @param checkout_id The ID of the checkout.
   * @param checkout_uri The URI to redirect users to when it's time for them to complete the checkout flow.
   * @param state The current state of the checkout.
  **/
  case class CheckoutResponse(checkout_id:Long, checkout_uri:Option[String] = None, state:Option[String] = None)

  /**
   * Case class representing a payment method for use with the tokenization API (CreditCard).
   *
   * @param payment_method_id The ID of the payment method. Should be the credit_card_id of the CC.
   * @param payment_method_type Defaulted to "credit_card" for now. (I don't know of any other values.)
  **/
  case class CheckoutPaymentMethod(payment_method_id:Long, payment_method_type:String = "credit_card")

  /**
   * Case class describing the URIs used by a checkout.
   *
   * @param redirect_uri The URI the user will be directed to after Checkout is completed.
   * @param callback_uri The URI that IPN notifications will be sent to.
  **/
  case class CheckoutUris(redirect_uri:Option[String] = None, callback_uri:Option[String] = None)

  /**
   * The JSON Serializer and deserializer for the Checkout case class.
   *
   * Unfortunately, this is a side effect of having more than 22 parameters and needing to split things
   * up a bit.
  **/
  object CheckoutSerializer extends Serializer[Checkout] {
    private val Class = classOf[Checkout]

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Checkout] = {
      case (TypeInfo(Class, _), json) =>
        //TODO
        Checkout(1234, "test", "blah", 10)
    }

    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case x:Checkout =>
        ("account_id" -> x.account_id) ~
        ("short_description" -> x.short_description) ~
        ("type" -> x.`type`) ~
        ("amount" -> x.amount) ~
        ("checkout_id" -> x.checkout_id) ~
        ("long_description" -> x.long_description) ~
        ("payer_email_message" -> x.payer_email_message) ~
        ("payee_email_message" -> x.payee_email_message) ~
        ("reference_id" -> x.reference_id) ~
        ("app_fee" -> x.app_fee) ~
        ("fee_payer" -> x.fee_payer) ~
        ("redirect_uri" -> x.uris.map(_.redirect_uri)) ~
        ("callback_uri" -> x.uris.map(_.callback_uri)) ~
        ("auto_capture" -> x.auto_capture) ~
        ("require_shipping" -> x.require_shipping) ~
        ("shipping_fee" -> x.shipping_fee) ~
        ("charge_tax" -> x.charge_tax) ~
        ("mode" -> x.mode) ~
        ("preapproval_id" -> x.preapproval_id) ~
        ("prefill_info" -> x.prefill_info) ~
        ("funding_sources" -> x.funding_sources) ~
        ("state" -> x.state) ~
        ("payment_method_id" -> x.payment_method.map(_.payment_method_id)) ~
        ("payment_method_type" -> x.payment_method.map(_.payment_method_type))
    }
  }

  /**
   * An instance of the Checkout class. Used to represent an actual exchange of funds between two parties.
   *
   * @param account_id The Account ID the checkout is associated with.
   * @param short_description Short description of the purchase.
   * @param type The type of transaction. One of: GOODS, SERVICE, DONATION, or PERSONAL.
   * @param amount The amount of the trnasaction.
   * @param checkout_id The ID assigned to the checkout by WePay. Your app should almost never set this value.
   * @param long_description A long description of the purchase.
   * @param payer_email_message A message from you that will be included in the confirmation email to the payer.
   * @param payee_email_message A message from you that will be included in the confirmation email to the payee.
   * @param reference_id The reference ID for the checkout. Should be unique per checkout per app.
   * @param app_fee The fee, in dollars and cents, your application will collect on this transaction. Limited to 20% of total amount.
   * @param fee_payer The person who pays transaction fee. One of "Payee" or "Payer". Defaults to "Payer".
   * @param uris A specification of the redirect_uri and callback_uri, if needed.
   * @param auto_capture Sets whether or not the payment should be captured instantly. Defaults to true.
   * @param require_shipping If true, payer will be required to enter a shipping address. Defaults to false.
   * @param shipping_fee The fee for shipping.
   * @param charge_tax Determines whether or not tax will be charged.
   * @param mode The mode the checkout will be displayed in. One of "regular" or "iframe". Defaults to "regular".
   * @param preapproval_id The preapproval ID associated with the checkout, if any.
   * @param prefill_info A JObject containing any information to prepopulate on WePay. Fields are: 'name', 'email', 'phone_number', 'address', 'city', 'state', 'zip'.
   * @param funding_sources Setting to determine what funding sources are allowed. Values are "bank,cc", "bank", or "cc".
   * @param state The state of the checkout.
   * @param payment_method The payment method information.
   * @define THIS Checkout
  **/
  case class Checkout(account_id:Long, short_description:String, `type`:String, amount:Double, checkout_id:Option[Long] = None,
                      long_description:Option[String] = None, payer_email_message:Option[String] = None,
                      payee_email_message:Option[String] = None, reference_id:Option[String] = None,
                      app_fee:Option[Double] = None, fee_payer:Option[String] = None, uris:Option[CheckoutUris] = None,
                      auto_capture:Option[Boolean] = None,
                      require_shipping:Option[Boolean] = None, shipping_fee:Option[Double] = None,
                      charge_tax:Option[Boolean] = None, mode:Option[String] = None, preapproval_id:Option[Long] = None,
                      prefill_info:Option[JObject] = None, funding_sources:Option[String] = None,
                      payment_method:Option[CheckoutPaymentMethod] = None,
                      state:Option[String] = None) extends ImmutableWePayResource[Checkout, CheckoutResponse] {
    val meta = Checkout
    val _id = checkout_id

    /**
     * Cancel the checkout, specifying a reason why.
     *
     * @param cancel_reason The reason the checkout is being canceled.
    **/
    def cancel(cancel_reason:String)(implicit authorizationToken:Option[WePayToken]) = {
      for {
        checkout_id <- (checkout_id:Box[Long]) ?~! "You can't cancel a checkout with no ID."
        response <- meta.cancel(checkout_id, cancel_reason)
      } yield {
        response
      }
    }

    /**
     * Refund the checkout, specifying the reason and possibly an amount for a partial refund.
     *
     * @param refund_reason The reason the checkout is being refunded.
     * @param amount The amount of the refund. If set to None, it will be a full refund.
    **/
    def refund(refund_reason:String, amount:Option[Double])(implicit authorizationToken:Option[WePayToken]) = {
      for {
        checkout_id <- (checkout_id:Box[Long]) ?~! "You can't refund a checkout with no ID."
        response <- meta.refund(checkout_id, refund_reason, amount)
      } yield {
        response
      }
    }

    /**
     * Capture the payment for this checkout, if auto_capture was set to false when it was created.
    **/
    def capture(implicit authorizationToken:Option[WePayToken]) = {
      for {
        checkout_id <- (checkout_id:Box[Long]) ?~! "You can't cancel a checkout with no ID."
        response <- meta.capture(checkout_id)
      } yield {
        response
      }
    }
  }

  /**
   * Meta object for finding and manipulating Checkout instances on WePay.
   *
   * @define INSTANCE Checkout
   * @define CRUDRESPONSETYPE CheckoutResponse
  **/
  object Checkout extends ImmutableWePayResourceMeta[Checkout, CheckoutResponse] {
    protected def extract(json:JValue) = json.extract[Checkout]
    protected def extractFindResults(json:JValue) = json.extract[List[Checkout]]
    protected def extractCrudResponse(json:JValue) = json.extract[CheckoutResponse]

    /**
     * Find a checkout within an account searching on various parameters.
     *
     * @param account_id The account to search.
     * @param start The start index to search from.
     * @param limit The maximum number of results to return.
     * @param reference_id The reference ID of the checkout to find.
     * @param state The state of the checkout to find.
     * @param preapproval_id The preapproval used with the checkout you're looking for.
    **/
    def find(account_id:String, start:Option[Int] = None, limit:Option[Int] = None, reference_id:Option[String] = None,
             state:Option[String] = None, preapproval_id:Option[Long] = None)(implicit authorizationToken:Option[WePayToken]) = {
      findQuery(
        ("account_id" -> account_id) ~
        ("start" -> start) ~
        ("limit" -> limit) ~
        ("reference_id" -> reference_id) ~
        ("state" -> state) ~
        ("preapproval_id" -> preapproval_id)
      )
    }

    /**
     * Cancel a checkout by ID. Useful if you don't already have the checkout in memory.
     *
     * @param checkout_id The ID of the checkout to cancel.
     * @param cancel_reason The reason for canceling the checkout.
    **/
    def cancel(checkout_id:Long, cancel_reason:String)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("cancel"), ("checkout_id" -> checkout_id) ~ ("cancel_reason" -> cancel_reason))
    }

    /**
     * Refund a checkout by ID. Useful if you don't already have the checkout in memory.
     *
     * @param checkout_id The ID of the checkout to refund.
     * @param refund_reason The reason for refunding the checkout.
     * @param amount The amount of the refund, if this is a partial refund.
    **/
    def refund(checkout_id:Long, refund_reason:String, amount:Option[Double])(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("refund"),
        ("checkout_id" -> checkout_id) ~
        ("refund_reason" -> refund_reason) ~
        ("amount" -> amount)
      )
    }

    /**
     * Capture funds for a checkout that was not previously captured.
     *
     * @param checkout_id The ID of the checkout to capture.
    **/
    def capture(checkout_id:Long)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("capture"), ("checkout_id" -> checkout_id))
    }
  }
}
