package me.frmr.wepay.api {
  import scala.concurrent.Future

  import net.liftweb.json._
    import JsonDSL._
  import net.liftweb.common.Box

  import me.frmr.wepay._
    import WePayHelpers._

  /**
   * The response type for Account API operations that don't return an Account, or some variation thereof.
   *
   * @param accountId The Account ID of the account being considered.
   * @param name The name of the account being considered.
   * @param description The description attached to the account.
   * @param referenceId The reference ID assigne to the account by your application. Reference IDs must be unique per user per app.
   * @param accountUri The URI to redirect the user to to view their account.
   * @param paymentLimit The maximum individual payment that can be made to this account. (deprecated, I think)
   * @param gaqDomains A list of analytics domains assigned to this account, if any.
   * @param pendingBalance The amount on the account that is pending.
   * @param availableBalance The amount on the account that is available.
   * @param currency The currency of the account.
  **/
  case class AccountResponse(accountId:Option[Long] = None, name:Option[String] = None, description:Option[String] = None,
                             referenceId:Option[String] = None, accountUri:Option[String] = None,
                             paymentLimit:Option[Long] = None, gaqDomains:Option[List[String]] = None,
                             pendingBalance:Option[Double] = None, availableBalance:Option[Double] = None,
                             currency:Option[String] = None)

  /**
   * An instance of an Account.
   *
   * Users on WePay can have multiple Accounts. API Applications can only manage accounts that they have created.
   *
   * @param name The name of the account.
   * @param description The description of the account.
   * @param accountId The Account ID, as determined by WePay. Your application should rarely need to set this value itself.
   * @param referenceId A unique identifier of the account to your application. Must be unique per user per app.
   * @param imageUri The URI to an image for the account.
   * @param paymentLimit The maximum individual payment that can be made to this account. (deprecated, I think)
   * @param gaqDomains Domain for analytics tracking.
   * @define THIS Account
  **/
  case class Account(name:String, description:String, accountId:Option[Long] = None, referenceId:Option[String] = None,
                     imageUri:Option[String] = None, paymentLimit:Option[Long] = None,
                     gaqDomains:Option[List[String]] = None) extends MutableWePayResource[Account, AccountResponse] {

    val meta = Account
    val _id = accountId

    /**
     * Retrieve the current balance on the account.
    **/
    def balance(implicit authorizationToken:Option[WePayToken]) = {
      unwrapBoxOfFuture {
        for {
          accountId <- (accountId:Box[Long]) ?~! "You can't retrieve the balance of an account with no ID."
        } yield {
          meta.balance(accountId)
        }
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
      unwrapBoxOfFuture {
        for {
          accountId <- (accountId:Box[Long]) ?~! "You can't set tax information on an account with no ID."
        } yield {
          meta.setTax(accountId, taxes)
        }
      }
    }

    /**
     * Retrieve tax information for an account.
     *
     * Tax information will be returned as a JArray of JObjects in the same format that
     * the information is passed in for setTax.
    **/
    def getTax(implicit authorizationToken:Option[WePayToken]) = {
      unwrapBoxOfFuture {
        for {
          accountId <- (accountId:Box[Long]) ?~! "You can't get tax information on an account with no ID."
        } yield {
          meta.getTax(accountId)
        }
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
    def find(name:Option[String] = None, referenceId:Option[String] = None)(implicit authorizationToken:Option[WePayToken]) = {
      findQuery(("name" -> name) ~ ("referenceId" -> referenceId))
    }

    /**
     * Retrieve the balance of an account by ID.
     *
     * This method is useful if you don't already have an account instance prepared, but still
     * want to check the account balance.
     *
     * @param accountId The ID of the account you wish to check.
    **/
    def balance(accountId:Long)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("balance"), ("accountId" -> accountId))
    }

    /**
     * Set the tax settings on an account by ID.
     *
     * This method is useful if you don't already have an account instance prepared, but still
     * want to set the tax settings. Tax settings are passed in as described in Account.setTax().
     *
     * @param accountId The account ID for which you would like to set the tax settings.
     * @param taxes The tax settings you wish to set.
    **/
    def setTax(accountId:Long, taxes:JArray)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("set_tax"), ("accountId" -> accountId) ~ ("taxes" -> taxes))
    }

    /**
     * Retrieve tax settings for an account by ID.
     *
     * This method is useful for checking the tax settings on an account when all
     * you have is the ID.
     *
     * @param accountId The Account ID for which you would like to check the tax settings.
    **/
    def getTax(accountId:Long)(implicit authorizationToken:Option[WePayToken]) = {
      resultRetrievalQuery(Some("get_tax"), ("accountId" -> accountId))
    }
  }
}
