package me.frmr.wepay.api {
  import net.liftweb.json._
    import JsonDSL._

  import me.frmr.wepay._

  case class AccountResponse(account_id:Option[Long] = None, name:Option[String] = None, description:Option[String] = None,
                             reference_id:Option[String] = None, account_uri:Option[String] = None,
                             payment_limit:Option[Long] = None, gaq_domains:Option[List[String]] = None,
                             pending_balance:Option[Double] = None, available_balance:Option[Double] = None,
                             currency:Option[String] = None)

  case class Account(name:String, description:String, account_id:Option[Long] = None, reference_id:Option[String] = None,
                     image_uri:Option[String] = None, payment_limit:Option[Long] = None,
                     gaq_domains:Option[List[String]] = None) extends MutableWePayResource[Account, AccountResponse] {

    val meta = Account
    val _id = account_id

    def balance(implicit authorizationToken:Option[WePayToken]) = {
      for {
        account_id <- account_id
      } {
        meta.balance(account_id)
      }
    }

    def setTax(taxes:JArray)(implicit authorizationToken:Option[WePayToken]) = {
      for {
        account_id <- account_id
      } {
        meta.setTax(account_id, taxes)
      }
    }

    def getTax(implicit authorizationToken:Option[WePayToken]) = {
      for {
        account_id <- account_id
      } {
        meta.getTax(account_id)
      }
    }
  }

  object Account extends MutableWePayResourceMeta[Account, AccountResponse] {
    def extract(json:JValue) = json.extract[Account]
    def extractFindResults(json:JValue) = json.extract[List[Account]]
    def extractCrudResponse(json:JValue) = json.extract[AccountResponse]

    def find(name:Option[String] = None, reference_id:Option[String] = None)(implicit authorizationToken:Option[WePayToken]) = {
      findQuery(("name" -> name) ~ ("reference_id" -> reference_id))
    }

    def balance(account_id:Long)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("balance"), ("account_id" -> account_id))
    }

    def setTax(account_id:Long, taxes:JArray)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("set_tax"), ("account_id" -> account_id) ~ ("taxes" -> taxes))
    }

    def getTax(account_id:Long)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("get_tax"), ("account_id" -> account_id))
    }
  }
}
