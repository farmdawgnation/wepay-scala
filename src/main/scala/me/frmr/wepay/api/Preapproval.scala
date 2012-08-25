package me.frmr.wepay.api {
  import net.liftweb.json._
    import JsonDSL._

  import org.joda.time.DateTime

  import me.frmr.wepay._

  case class PreapprovalResponse(preapproval_id:Long, preapproval_uri:Option[String] = None,
                                 state:Option[String] = None)

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

    def cancel(implicit authorizationToken:Option[WePayToken]) = {
      meta.cancel(preapproval_id getOrElse 0)
    }
  }

  object Preapproval extends ImmutableWePayResourceMeta[Preapproval, PreapprovalResponse] {
    def extract(json:JValue) = json.extract[Preapproval]
    def extractFindResults(json:JValue) = json.extract[List[Preapproval]]
    def extractCrudResponse(json:JValue) = json.extract[PreapprovalResponse]

    def find(state:Option[String] = None, reference_id:Option[String] = None)(implicit authorizationToken:Option[WePayToken]) = {
      findQuery(
        ("state" -> state) ~
        ("reference_id" -> reference_id)
      )
    }

    def cancel(preapproval_id:Long)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("cancel"), ("preapproval_id" -> preapproval_id))
    }
  }

}