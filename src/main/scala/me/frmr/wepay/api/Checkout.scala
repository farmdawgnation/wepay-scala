package me.frmr.wepay.api {
  import net.liftweb.json._
    import JsonDSL._
    import Serialization._
  import net.liftweb.common.Box

  import me.frmr.wepay._
    import WePayHelpers._

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
   * Case class representing a payment authorization from the preapproval or tokenization API.
   *
   * @param preapproval_id The ID of the preapproval that authorizes this checkout.
   * @param payment_method_id The ID of the payment method from CC tokenization. Should be the credit_card_id of the CC.
   * @param payment_method_type The type used for CC tokenization API (should be "credit_card").
  **/
  case class CheckoutAuthorization(preapproval_id:Option[Long] = None,
                                   payment_method_id:Option[Long] = None,
                                   payment_method_type:Option[String] = None)

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
        val authorization = {
          val preapproval_id = (json \ "preapproval_id").extract[Option[Long]]
          val payment_method_id = (json \ "payment_method_id").extract[Option[Long]]
          val payment_method_type = (json \ "payment_method_type").extract[Option[String]]

          (preapproval_id, payment_method_id, payment_method_type) match {
            case (None, payment_method_id:Some[Long], payment_method_type:Some[String]) =>
              Some(CheckoutAuthorization(None, payment_method_id, payment_method_type))
            case (preapproval_id:Some[Long], None, None) =>
              Some(CheckoutAuthorization(preapproval_id, None, None))
            case _ => None
          }
        }

        Checkout(
          account_id = (json \ "account_id").extract[Long],
          short_description = (json \ "short_description").extract[String],
          `type` = (json \ "type").extract[String],
          amount = (json \ "amount").extract[Double],
          checkout_id = (json \ "checkout_id").extract[Option[Long]],
          long_description = (json \ "long_description").extract[Option[String]],
          payer_email_message = (json \ "payer_email_message").extract[Option[String]],
          payee_email_message = (json \ "payee_email_message").extract[Option[String]],
          reference_id = (json \ "reference_id").extract[Option[String]],
          app_fee = (json \ "app_fee").extract[Option[Double]],
          fee_payer = (json \ "fee_payer").extract[Option[String]],
          redirect_uri = (json \ "redirect_uri").extract[Option[String]],
          callback_uri = (json \ "callback_uri").extract[Option[String]],
          require_shipping = (json \ "require_shipping").extract[Option[Boolean]],
          shipping_fee = (json \ "shipping_fee").extract[Option[Double]],
          charge_tax = (json \ "charge_tax").extract[Option[Boolean]],
          mode = (json \ "mode").extract[Option[String]],
          prefill_info = (json \ "prefill_info").extract[Option[JObject]],
          funding_sources = (json \ "funding_sources").extract[Option[String]],
          state = (json \ "state").extract[Option[String]],
          authorization = authorization
        )
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
        ("redirect_uri" -> x.redirect_uri) ~
        ("callback_uri" -> x.callback_uri) ~
        ("auto_capture" -> x.auto_capture) ~
        ("require_shipping" -> x.require_shipping) ~
        ("shipping_fee" -> x.shipping_fee) ~
        ("charge_tax" -> x.charge_tax) ~
        ("mode" -> x.mode) ~
        ("preapproval_id" -> x.authorization.flatMap(_.preapproval_id)) ~
        ("prefill_info" -> x.prefill_info) ~
        ("funding_sources" -> x.funding_sources) ~
        ("state" -> x.state) ~
        ("payment_method_id" -> x.authorization.flatMap(_.payment_method_id)) ~
        ("payment_method_type" -> x.authorization.flatMap(_.payment_method_type))
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
   * @param redirect_uri The URI the user will be redirected to upon completing or canceling the checkout.
   * @param callback_uri The URI that IPNs will be sent to.
   * @param auto_capture Sets whether or not the payment should be captured instantly. Defaults to true.
   * @param require_shipping If true, payer will be required to enter a shipping address. Defaults to false.
   * @param shipping_fee The fee for shipping.
   * @param charge_tax Determines whether or not tax will be charged.
   * @param mode The mode the checkout will be displayed in. One of "regular" or "iframe". Defaults to "regular".
   * @param prefill_info A JObject containing any information to prepopulate on WePay. Fields are: 'name', 'email', 'phone_number', 'address', 'city', 'state', 'zip'.
   * @param funding_sources Setting to determine what funding sources are allowed. Values are "bank,cc", "bank", or "cc".
   * @param state The state of the checkout.
   * @param authorization The authorization information for pre-authorized checkouts.
   * @define THIS Checkout
  **/
  case class Checkout(account_id:Long, short_description:String, `type`:String, amount:Double, checkout_id:Option[Long] = None,
                      long_description:Option[String] = None, payer_email_message:Option[String] = None,
                      payee_email_message:Option[String] = None, reference_id:Option[String] = None,
                      app_fee:Option[Double] = None, fee_payer:Option[String] = None,
                      redirect_uri:Option[String] = None, callback_uri:Option[String] = None,
                      auto_capture:Option[Boolean] = None,
                      require_shipping:Option[Boolean] = None, shipping_fee:Option[Double] = None,
                      charge_tax:Option[Boolean] = None, mode:Option[String] = None,
                      prefill_info:Option[JObject] = None, funding_sources:Option[String] = None,
                      authorization:Option[CheckoutAuthorization] = None,
                      state:Option[String] = None) extends ImmutableWePayResource[Checkout, CheckoutResponse] {
    val meta = Checkout
    val _id = checkout_id

    /**
     * Cancel the checkout, specifying a reason why.
     *
     * @param cancel_reason The reason the checkout is being canceled.
    **/
    def cancel(cancel_reason:String)(implicit authorizationToken:Option[WePayToken]) = {
      unwrapBoxOfFuture {
        for {
          checkout_id <- (checkout_id:Box[Long]) ?~! "You can't cancel a checkout with no ID."
        } yield {
          meta.cancel(checkout_id, cancel_reason)
        }
      }
    }

    /**
     * Refund the checkout, specifying the reason and possibly an amount for a partial refund.
     *
     * @param refund_reason The reason the checkout is being refunded.
     * @param amount The amount of the refund. If set to None, it will be a full refund.
    **/
    def refund(refund_reason:String, amount:Option[Double])(implicit authorizationToken:Option[WePayToken]) = {
      unwrapBoxOfFuture {
        for {
          checkout_id <- (checkout_id:Box[Long]) ?~! "You can't refund a checkout with no ID."
        } yield {
          meta.refund(checkout_id, refund_reason, amount)
        }
      }
    }

    /**
     * Capture the payment for this checkout, if auto_capture was set to false when it was created.
    **/
    def capture(implicit authorizationToken:Option[WePayToken]) = {
      unwrapBoxOfFuture {
        for {
          checkout_id <- (checkout_id:Box[Long]) ?~! "You can't cancel a checkout with no ID."
        } yield {
          meta.capture(checkout_id)
        }
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
    override implicit val formats = DefaultFormats + CheckoutSerializer

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
