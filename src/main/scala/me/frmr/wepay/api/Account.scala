package me.frmr.wepay.api {
  import net.liftweb.json._
    import JsonDSL._

  import me.frmr.wepay._

  /**
   * The response type for Account API operations that don't return an Account, or some variation thereof.
   *
   * @param account_id The Account ID of the account being considered.
   * @param name The name of the account being considered.
   * @param description The description attached to the account.
   * @param reference_id The reference ID assigne to the account by your application. Reference IDs must be unique per user per app.
   * @param account_uri The URI to redirect the user to to view their account.
   * @param payment_limit The maximum individual payment that can be made to this account. (deprecated, I think)
   * @param gaq_domains A list of analytics domains assigned to this account, if any.
   * @param pending_balance The amount on the account that is pending.
   * @param available_balance The amount on the account that is available.
   * @param currency The currency of the account.
  **/
  case class AccountResponse(account_id:Option[Long] = None, name:Option[String] = None, description:Option[String] = None,
                             reference_id:Option[String] = None, account_uri:Option[String] = None,
                             payment_limit:Option[Long] = None, gaq_domains:Option[List[String]] = None,
                             pending_balance:Option[Double] = None, available_balance:Option[Double] = None,
                             currency:Option[String] = None)

  /**
   * An instance of an Account.
   *
   * Users on WePay can have multiple Accounts. API Applications can only manage accounts that they have created.
   *
   * @define THIS Account
   * @param name The name of the account.
   * @param description The description of the account.
   * @param account_id The Account ID, as determined by WePay. Your application should rarely need to set this value itself.
   * @param reference_id A unique identifier of the account to your application. Must be unique per user per app.
   * @param image_uri The URI to an image for the account.
   * @param payment_limit The maximum individual payment that can be made to this account. (deprecated, I think)
   * @param gaq_domains Domain for analytics tracking.
  **/
  case class Account(name:String, description:String, account_id:Option[Long] = None, reference_id:Option[String] = None,
                     image_uri:Option[String] = None, payment_limit:Option[Long] = None,
                     gaq_domains:Option[List[String]] = None) extends MutableWePayResource[Account, AccountResponse] {

    val meta = Account
    val _id = account_id

    /**
     * Retrieve the current balance on the account.
    **/
    def balance(implicit authorizationToken:Option[WePayToken]) = {
      for {
        account_id <- account_id
      } {
        meta.balance(account_id)
      }
    }

    /**
     * Set the tax settings for the account.
     *
     * The taxes object should be a List of JObjects containing the relevant tax percentage and what
     * locale, if any, it applies to. To implement the example presented in the
     * [[https://www.wepay.com/developer/reference/account#set_tax WePay documentation for set_tax]],
     * you would make the following call.
     *
     * {{{
     *   accountInstance.setTax(List(
     *     ("percent" -> 10) ~ ("country" -> "US") ~ ("state" -> "CA") ~ ("zip" -> "94025"),
     *     ("percent" -> 7) ~ ("country" -> "US") ~ ("state" -> "CA"),
     *     ("percent" -> 5) ~ ("country" -> "US")
     *   ))
     * }}}
     *
     * @param taxes The List describing the taxes you want to set for the account.
    **/
    def setTax(taxes:JArray)(implicit authorizationToken:Option[WePayToken]) = {
      for {
        account_id <- account_id
      } {
        meta.setTax(account_id, taxes)
      }
    }

    /**
     * Retrieve tax information for an account.
     *
     * Tax information will be returned as a JArray of JObjects in the same format that
     * the information is passed in for setTax.
    **/
    def getTax(implicit authorizationToken:Option[WePayToken]) = {
      for {
        account_id <- account_id
      } {
        meta.getTax(account_id)
      }
    }
  }

  /**
   * Meta object for finding and manipulating Account instances on WePay.
   *
   * @define INSTANCE Account
   * @define CRUDRESPONSETYPE AccountResponse
  **/
  object Account extends MutableWePayResourceMeta[Account, AccountResponse] {
    protected def extract(json:JValue) = json.extract[Account]
    protected def extractFindResults(json:JValue) = json.extract[List[Account]]
    protected def extractCrudResponse(json:JValue) = json.extract[AccountResponse]

    /**
     * Find an account by name or reference_id. One of the two fields must be specified, or WePay
     * will return a failure.
     *
     * @param name The name of the account to search for.
     * @param reference_id The Reference ID of the account to search for.
    **/
    def find(name:Option[String] = None, reference_id:Option[String] = None)(implicit authorizationToken:Option[WePayToken]) = {
      findQuery(("name" -> name) ~ ("reference_id" -> reference_id))
    }

    /**
     * Retrieve the balance of an account by ID.
     *
     * This method is useful if you don't already have an account instance prepared, but still
     * want to check the account balance.
     *
     * @param account_id The ID of the account you wish to check.
    **/
    def balance(account_id:Long)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("balance"), ("account_id" -> account_id))
    }

    /**
     * Set the tax settings on an account by ID.
     *
     * This method is useful if you don't already have an account instance prepared, but still
     * want to set the tax settings. Tax settings are passed in as described in Account.setTax().
     *
     * @param account_id The account ID for which you would like to set the tax settings.
     * @param taxes The tax settings you wish to set.
    **/
    def setTax(account_id:Long, taxes:JArray)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("set_tax"), ("account_id" -> account_id) ~ ("taxes" -> taxes))
    }

    /**
     * Retrieve tax settings for an account by ID.
     *
     * This method is useful for checking the tax settings on an account when all
     * you have is the ID.
     *
     * @param account_id The Account ID for which you would like to check the tax settings.
    **/
    def getTax(account_id:Long)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("get_tax"), ("account_id" -> account_id))
    }
  }
}
