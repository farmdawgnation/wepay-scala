package me.frmr.wepay.api {
  import net.liftweb.json._
    import JsonDSL._
  import net.liftweb.common.Box

  import org.joda.time.DateTime

  import me.frmr.wepay._
    import WePayHelpers._

  /**
   * The response type for Preapproval operations that don't return an instance of Preapproval.
   *
   * @param preapproval_id The ID of the preapproval, assigned by WePay.
   * @param preapproval_uri The URI to direct the user to when they need to authorize the preapproval.
   * @param state The state of the Preapproval.
  **/
  case class PreapprovalResponse(preapproval_id:Long, preapproval_uri:Option[String] = None,
                                 state:Option[String] = None)

  /**
   * An instance of a WePay Preapproval.
   *
   * Preapprovals are used to, well, preapprove payments within a certain date range and amount.
   * Useful for kickstarter-like applications or subscription-based services.
   *
   * @param account_id The Account ID that will be receiving the payment from this preapproval.
   * @param short_description The short description of what the user will be paying for.
   * @param period How frequently the user can be charged. Can be: hourly, daily, weekly, biweekly, monthly, bimonthly, quarterly, yearly, or once.
   * @param preapproval_id The preapproval ID assigned by WePay. Should rarely ever be set by your app.
   * @param reference_id The reference ID of the preapproval. Should be unique per app per account.
   * @param app_fee The fee your application will take on this transaction.
   * @param fee_payer The person who pays transaction fee. One of "Payee" or "Payer". Defaults to "Payer".
   * @param redirect_uri The URI that the user will be redirected to for completing the Preapproval flow.
   * @param callback_uri The URI that IPN notifications will be directed to.
   * @param require_shipping If true, the user will be required to enter shipping information.
   * @param shipping_fee The fee for shipping.
   * @param charge_tax If set to true, tax will be charged.
   * @param payer_email_message A message from you that will be included in the confirmation email to the payer.
   * @param payee_email_message A message from you that will be included in the confirmation email to the payee.
   * @param long_description The long description of the transaction.
   * @param frequency The number of times this preapproval can be used per period.
   * @param start_time When the API can start charging using this preapproval.
   * @param end_time The date the API can no longer charge using this preapproval.
   * @param auto_recur Determines whether or not this is an auto-recurring Peapproval.
   * @param state The state of the Preapproval.
   * @param mode The mode the preapproval will run in. Can be "regular" or "iframe". Defaults to "regular".
   * @define THIS Preapproval
  **/
  case class Preapproval(account_id:Long, amount:Double, short_description:String, period:String,
                         preapproval_id:Option[Long] = None, reference_id:Option[String] = None,
                         app_fee:Option[Double] = None, fee_payer:Option[String] = None,
                         redirect_uri:Option[String] = None, callback_uri:Option[String] = None,
                         require_shipping:Option[Boolean] = None, shipping_fee:Option[Double] = None,
                         charge_tax:Option[Boolean] = None, payer_email_message:Option[String] = None,
                         payee_email_message:Option[String] = None, long_description:Option[String] = None,
                         frequency:Option[Int] = None, start_time:Option[DateTime] = None,
                         end_time:Option[DateTime] = None, auto_recur:Option[Boolean] = None,
                         state:Option[String] = None, mode:Option[String] = None)
                         extends ImmutableWePayResource[Preapproval, PreapprovalResponse] {

    val meta = Preapproval
    val _id = preapproval_id

    /**
     * Cancel the preapproval.
    **/
    def cancel(implicit authorizationToken:Option[WePayToken]) = {
      unwrapBoxOfFuture {
        for {
          id <- (preapproval_id:Box[Long]) ?~! "You can't cancel a preapproval with no ID."
        } yield {
          meta.cancel(id)
        }
      }
    }
  }

  /**
   * The meta object for finding and manipulating Preapproval instances.
   *
   * @define INSTANCE Preapproval
   * @define CRUDRESPONSETYPE PreapprovalResponse
  **/
  object Preapproval extends ImmutableWePayResourceMeta[Preapproval, PreapprovalResponse] {
    protected def extract(json:JValue) = json.extract[Preapproval]
    protected def extractFindResults(json:JValue) = json.extract[List[Preapproval]]
    protected def extractCrudResponse(json:JValue) = json.extract[PreapprovalResponse]

    /**
     * Find a preapproval based on some search parameters.
     *
     * @param state The state of the preapproval you're looking for.
     * @param reference_id The reference ID of the preapproval you're looking for.
    **/
    def find(state:Option[String] = None, reference_id:Option[String] = None)(implicit authorizationToken:Option[WePayToken]) = {
      findQuery(
        ("state" -> state) ~
        ("reference_id" -> reference_id)
      )
    }

    /**
     * Cancel a preapproval by ID. Useful if you don't already have an instance avaialable.
     *
     * @param preapproval_id The ID of the preapproval to cancel.
    **/
    def cancel(preapproval_id:Long)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("cancel"), ("preapproval_id" -> preapproval_id))
    }
  }

}
