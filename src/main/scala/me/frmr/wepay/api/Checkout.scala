package me.frmr.wepay.api {
  import net.liftweb.json._
    import JsonDSL._

  import me.frmr.wepay._

  /**
   * The response type for API operations that don't return an instance of Checkout, or some variation
   * thereof.
  **/
  case class CheckoutResponse(checkout_id:Long, checkout_uri:Option[String] = None, state:Option[String] = None)

  /**
   * An instance of the Checkout class. Used to represent an actual exchange of funds between two parties.
   *
   * @define THIS Checkout
  **/
  case class Checkout(account_id:Long, short_description:String, `type`:String, amount:Double, checkout_id:Option[Long] = None,
                      long_description:Option[String] = None, payer_email_message:Option[String] = None,
                      payee_email_message:Option[String] = None, reference_id:Option[String] = None,
                      app_fee:Option[Double] = None, fee_payer:Option[String] = None, redirect_uri:Option[String] = None,
                      callback_uri:Option[String] = None, auto_capture:Option[Boolean] = None,
                      require_shipping:Option[Boolean] = None, shipping_fee:Option[Double] = None,
                      charge_tax:Option[Boolean] = None, mode:Option[String] = None, preapproval_id:Option[Long] = None,
                      prefill_info:Option[JObject] = None, funding_sources:Option[String] = None,
                      state:Option[String] = None) extends ImmutableWePayResource[Checkout, CheckoutResponse] {
    val meta = Checkout
    val _id = checkout_id

    def cancel(cancel_reason:String)(implicit authorizationToken:Option[WePayToken]) = {
      for {
        checkout_id <- checkout_id
      } {
        meta.cancel(checkout_id, cancel_reason)
      }
    }

    def refund(refund_reason:String, amount:Option[Double])(implicit authorizationToken:Option[WePayToken]) = {
      for {
        checkout_id <- checkout_id
      } {
        meta.refund(checkout_id, refund_reason, amount)
      }
    }

    def capture(implicit authorizationToken:Option[WePayToken]) = {
      for {
        checkout_id <- checkout_id
      } {
        meta.capture(checkout_id)
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
    def extract(json:JValue) = json.extract[Checkout]
    def extractFindResults(json:JValue) = json.extract[List[Checkout]]
    def extractCrudResponse(json:JValue) = json.extract[CheckoutResponse]

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

    def cancel(checkout_id:Long, cancel_reason:String)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("cancel"), ("checkout_id" -> checkout_id) ~ ("cancel_reason" -> cancel_reason))
    }

    def refund(checkout_id:Long, refund_reason:String, amount:Option[Double])(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("refund"),
        ("checkout_id" -> checkout_id) ~
        ("refund_reason" -> refund_reason) ~
        ("amount" -> amount)
      )
    }

    def capture(checkout_id:Long)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("capture"), ("checkout_id" -> checkout_id))
    }
  }
}
