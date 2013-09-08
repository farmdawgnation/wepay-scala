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
   * @param preapprovalId The ID of the preapproval, assigned by WePay.
   * @param preapprovalUri The URI to direct the user to when they need to authorize the preapproval.
   * @param state The state of the Preapproval.
  **/
  case class PreapprovalResponse(preapprovalId:Long, preapprovalUri:Option[String] = None,
                                 state:Option[String] = None)

  /**
   * An instance of a WePay Preapproval.
   *
   * Preapprovals are used to, well, preapprove payments within a certain date range and amount.
   * Useful for kickstarter-like applications or subscription-based services.
   *
   * @param accountId The Account ID that will be receiving the payment from this preapproval.
   * @param shortDescription The short description of what the user will be paying for.
   * @param period How frequently the user can be charged. Can be: hourly, daily, weekly, biweekly, monthly, bimonthly, quarterly, yearly, or once.
   * @param preapprovalId The preapproval ID assigned by WePay. Should rarely ever be set by your app.
   * @param referenceId The reference ID of the preapproval. Should be unique per app per account.
   * @param appFee The fee your application will take on this transaction.
   * @param feePayer The person who pays transaction fee. One of "Payee" or "Payer". Defaults to "Payer".
   * @param redirectUri The URI that the user will be redirected to for completing the Preapproval flow.
   * @param callbackUri The URI that IPN notifications will be directed to.
   * @param requireShipping If true, the user will be required to enter shipping information.
   * @param shippingFee The fee for shipping.
   * @param chargeTax If set to true, tax will be charged.
   * @param payerEmailMessage A message from you that will be included in the confirmation email to the payer.
   * @param payeeEmailMessage A message from you that will be included in the confirmation email to the payee.
   * @param longDescription The long description of the transaction.
   * @param frequency The number of times this preapproval can be used per period.
   * @param startTime When the API can start charging using this preapproval.
   * @param endTime The date the API can no longer charge using this preapproval.
   * @param autoRecur Determines whether or not this is an auto-recurring Peapproval.
   * @param state The state of the Preapproval.
   * @param mode The mode the preapproval will run in. Can be "regular" or "iframe". Defaults to "regular".
   * @define THIS Preapproval
  **/
  case class Preapproval(accountId:Long, amount:Double, shortDescription:String, period:String,
                         preapprovalId:Option[Long] = None, referenceId:Option[String] = None,
                         appFee:Option[Double] = None, feePayer:Option[String] = None,
                         redirectUri:Option[String] = None, callbackUri:Option[String] = None,
                         requireShipping:Option[Boolean] = None, shippingFee:Option[Double] = None,
                         chargeTax:Option[Boolean] = None, payerEmailMessage:Option[String] = None,
                         payeeEmailMessage:Option[String] = None, longDescription:Option[String] = None,
                         frequency:Option[Int] = None, startTime:Option[DateTime] = None,
                         endTime:Option[DateTime] = None, autoRecur:Option[Boolean] = None,
                         state:Option[String] = None, mode:Option[String] = None)
                         extends ImmutableWePayResource[Preapproval, PreapprovalResponse] {

    val meta = Preapproval
    val _id = preapprovalId

    /**
     * Cancel the preapproval.
    **/
    def cancel(implicit authorizationToken:Option[WePayToken]) = {
      unwrapBoxOfFuture {
        for {
          id <- (preapprovalId:Box[Long]) ?~! "You can't cancel a preapproval with no ID."
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
     * @param referenceId The reference ID of the preapproval you're looking for.
    **/
    def find(state:Option[String] = None, referenceId:Option[String] = None)(implicit authorizationToken:Option[WePayToken]) = {
      findQuery(
        ("state" -> state) ~
        ("referenceId" -> referenceId)
      )
    }

    /**
     * Cancel a preapproval by ID. Useful if you don't already have an instance avaialable.
     *
     * @param preapprovalId The ID of the preapproval to cancel.
    **/
    def cancel(preapprovalId:Long)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("cancel"), ("preapprovalId" -> preapprovalId))
    }
  }

}
