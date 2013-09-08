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
   * @param checkoutId The ID of the checkout.
   * @param checkoutUri The URI to redirect users to when it's time for them to complete the checkout flow.
   * @param state The current state of the checkout.
  **/
  case class CheckoutResponse(checkoutId:Long, checkoutUri:Option[String] = None, state:Option[String] = None)

  /**
   * Case class representing a payment authorization from the preapproval or tokenization API.
   *
   * @param preapprovalId The ID of the preapproval that authorizes this checkout.
   * @param paymentMethodId The ID of the payment method from CC tokenization. Should be the credit_card_id of the CC.
   * @param paymentMethodType The type used for CC tokenization API (should be "credit_card").
  **/
  case class CheckoutAuthorization(preapprovalId: Option[Long] = None,
                                   paymentMethodId: Option[Long] = None,
                                   paymentMethodType: Option[String] = None)

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
          val preapprovalId = (json \ "preapprovalId").extract[Option[Long]]
          val paymentMethodId = (json \ "paymentMethodId").extract[Option[Long]]
          val paymentMethodType = (json \ "paymentMethodType").extract[Option[String]]

          (preapprovalId, paymentMethodId, paymentMethodType) match {
            case (None, paymentMethodId: Some[Long], paymentMethodType: Some[String]) =>
              Some(CheckoutAuthorization(None, paymentMethodId, paymentMethodType))

            case (preapprovalId:Some[Long], None, None) =>
              Some(CheckoutAuthorization(preapprovalId, None, None))

            case _ =>
              None
          }
        }

        Checkout(
          accountId = (json \ "accountId").extract[Long],
          shortDescription = (json \ "shortDescription").extract[String],
          `type` = (json \ "type").extract[String],
          amount = (json \ "amount").extract[Double],
          checkoutId = (json \ "checkoutId").extract[Option[Long]],
          longDescription = (json \ "longDescription").extract[Option[String]],
          payerEmailMessage = (json \ "payerEmailMessage").extract[Option[String]],
          payeeEmailMessage = (json \ "payeeEmailMessage").extract[Option[String]],
          referenceId = (json \ "referenceId").extract[Option[String]],
          appFee = (json \ "appFee").extract[Option[Double]],
          feePayer = (json \ "feePayer").extract[Option[String]],
          redirectUri = (json \ "redirectUri").extract[Option[String]],
          callbackUri = (json \ "callbackUri").extract[Option[String]],
          requireShipping = (json \ "requireShipping").extract[Option[Boolean]],
          shippingFee = (json \ "shippingFee").extract[Option[Double]],
          chargeTax = (json \ "chargeTax").extract[Option[Boolean]],
          mode = (json \ "mode").extract[Option[String]],
          prefillInfo = (json \ "prefillInfo").extract[Option[JObject]],
          fundingSources = (json \ "fundingSources").extract[Option[String]],
          state = (json \ "state").extract[Option[String]],
          authorization = authorization
        )
    }

    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case x:Checkout =>
        ("accountId" -> x.accountId) ~
        ("shortDescription" -> x.shortDescription) ~
        ("type" -> x.`type`) ~
        ("amount" -> x.amount) ~
        ("checkoutId" -> x.checkoutId) ~
        ("longDescription" -> x.longDescription) ~
        ("payerEmailMessage" -> x.payerEmailMessage) ~
        ("payeeEmailMessage" -> x.payeeEmailMessage) ~
        ("referenceId" -> x.referenceId) ~
        ("appFee" -> x.appFee) ~
        ("feePayer" -> x.feePayer) ~
        ("redirectUri" -> x.redirectUri) ~
        ("callbackUri" -> x.callbackUri) ~
        ("autoCapture" -> x.autoCapture) ~
        ("requireShipping" -> x.requireShipping) ~
        ("shippingFee" -> x.shippingFee) ~
        ("chargeTax" -> x.chargeTax) ~
        ("mode" -> x.mode) ~
        ("preapprovalId" -> x.authorization.flatMap(_.preapprovalId)) ~
        ("prefillInfo" -> x.prefillInfo) ~
        ("fundingSources" -> x.fundingSources) ~
        ("state" -> x.state) ~
        ("paymentMethodId" -> x.authorization.flatMap(_.paymentMethodId)) ~
        ("paymentMethodType" -> x.authorization.flatMap(_.paymentMethodType))
    }
  }

  /**
   * An instance of the Checkout class. Used to represent an actual exchange of funds between two parties.
   *
   * @param accountId The Account ID the checkout is associated with.
   * @param shortDescription Short description of the purchase.
   * @param type The type of transaction. One of: GOODS, SERVICE, DONATION, or PERSONAL.
   * @param amount The amount of the trnasaction.
   * @param checkoutId The ID assigned to the checkout by WePay. Your app should almost never set this value.
   * @param longDescription A long description of the purchase.
   * @param payerEmailMessage A message from you that will be included in the confirmation email to the payer.
   * @param payeeEmailMessage A message from you that will be included in the confirmation email to the payee.
   * @param referenceId The reference ID for the checkout. Should be unique per checkout per app.
   * @param appFee The fee, in dollars and cents, your application will collect on this transaction. Limited to 20% of total amount.
   * @param feePayer The person who pays transaction fee. One of "Payee" or "Payer". Defaults to "Payer".
   * @param redirectUri The URI the user will be redirected to upon completing or canceling the checkout.
   * @param callbackUri The URI that IPNs will be sent to.
   * @param autoCapture Sets whether or not the payment should be captured instantly. Defaults to true.
   * @param requireShipping If true, payer will be required to enter a shipping address. Defaults to false.
   * @param shippingFee The fee for shipping.
   * @param chargeTax Determines whether or not tax will be charged.
   * @param mode The mode the checkout will be displayed in. One of "regular" or "iframe". Defaults to "regular".
   * @param prefillInfo A JObject containing any information to prepopulate on WePay. Fields are: 'name', 'email', 'phone_number', 'address', 'city', 'state', 'zip'.
   * @param fundingSources Setting to determine what funding sources are allowed. Values are "bank,cc", "bank", or "cc".
   * @param state The state of the checkout.
   * @param authorization The authorization information for pre-authorized checkouts.
   * @define THIS Checkout
  **/
  case class Checkout(accountId:Long, shortDescription:String, `type`:String, amount:Double, checkoutId:Option[Long] = None,
                      longDescription:Option[String] = None, payerEmailMessage:Option[String] = None,
                      payeeEmailMessage:Option[String] = None, referenceId:Option[String] = None,
                      appFee:Option[Double] = None, feePayer:Option[String] = None,
                      redirectUri:Option[String] = None, callbackUri:Option[String] = None,
                      autoCapture:Option[Boolean] = None,
                      requireShipping:Option[Boolean] = None, shippingFee:Option[Double] = None,
                      chargeTax:Option[Boolean] = None, mode:Option[String] = None,
                      prefillInfo:Option[JObject] = None, fundingSources:Option[String] = None,
                      authorization:Option[CheckoutAuthorization] = None,
                      state:Option[String] = None) extends ImmutableWePayResource[Checkout, CheckoutResponse] {
    val meta = Checkout
    val _id = checkoutId

    /**
     * Cancel the checkout, specifying a reason why.
     *
     * @param cancelReason The reason the checkout is being canceled.
    **/
    def cancel(cancelReason:String)(implicit authorizationToken:Option[WePayToken]) = {
      unwrapBoxOfFuture {
        for {
          checkoutId <- (checkoutId: Box[Long]) ?~! "You can't cancel a checkout with no ID."
        } yield {
          meta.cancel(checkoutId, cancelReason)
        }
      }
    }

    /**
     * Refund the checkout, specifying the reason and possibly an amount for a partial refund.
     *
     * @param refundReason The reason the checkout is being refunded.
     * @param amount The amount of the refund. If set to None, it will be a full refund.
    **/
    def refund(refundReason:String, amount:Option[Double])(implicit authorizationToken:Option[WePayToken]) = {
      unwrapBoxOfFuture {
        for {
          checkoutId <- (checkoutId:Box[Long]) ?~! "You can't refund a checkout with no ID."
        } yield {
          meta.refund(checkoutId, refundReason, amount)
        }
      }
    }

    /**
     * Capture the payment for this checkout, if auto_capture was set to false when it was created.
    **/
    def capture(implicit authorizationToken:Option[WePayToken]) = {
      unwrapBoxOfFuture {
        for {
          checkoutId <- (checkoutId:Box[Long]) ?~! "You can't cancel a checkout with no ID."
        } yield {
          meta.capture(checkoutId)
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
     * @param accountId The account to search.
     * @param start The start index to search from.
     * @param limit The maximum number of results to return.
     * @param referenceId The reference ID of the checkout to find.
     * @param state The state of the checkout to find.
     * @param preapprovalId The preapproval used with the checkout you're looking for.
    **/
    def find(accountId:String, start:Option[Int] = None, limit:Option[Int] = None, referenceId:Option[String] = None,
             state:Option[String] = None, preapprovalId:Option[Long] = None)(implicit authorizationToken:Option[WePayToken]) = {
      findQuery(
        ("accountId" -> accountId) ~
        ("start" -> start) ~
        ("limit" -> limit) ~
        ("referenceId" -> referenceId) ~
        ("state" -> state) ~
        ("preapprovalId" -> preapprovalId)
      )
    }

    /**
     * Cancel a checkout by ID. Useful if you don't already have the checkout in memory.
     *
     * @param checkoutId The ID of the checkout to cancel.
     * @param cancelReason The reason for canceling the checkout.
    **/
    def cancel(checkoutId:Long, cancelReason:String)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("cancel"), ("checkoutId" -> checkoutId) ~ ("cancelReason" -> cancelReason))
    }

    /**
     * Refund a checkout by ID. Useful if you don't already have the checkout in memory.
     *
     * @param checkoutId The ID of the checkout to refund.
     * @param refundReason The reason for refunding the checkout.
     * @param amount The amount of the refund, if this is a partial refund.
    **/
    def refund(checkoutId:Long, refundReason:String, amount:Option[Double])(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("refund"),
        ("checkoutId" -> checkoutId) ~
        ("refundReason" -> refundReason) ~
        ("amount" -> amount)
      )
    }

    /**
     * Capture funds for a checkout that was not previously captured.
     *
     * @param checkoutId The ID of the checkout to capture.
    **/
    def capture(checkoutId:Long)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("capture"), ("checkoutId" -> checkoutId))
    }
  }
}
