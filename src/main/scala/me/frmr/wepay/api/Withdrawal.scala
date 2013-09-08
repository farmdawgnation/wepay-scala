package me.frmr.wepay.api {
  import net.liftweb.json._
    import JsonDSL._

  case class WithdrawalResponse(withdrawalId: Long, withdrawalUri: Option[String] = None)

  /**
   * Represents an instance of a Withdrawal.
   *
   * @param accountId The WePay Account ID associated with the Withdrawal.
   * @param amount The withdrawal amount.
   * @param redirectUri The URI to redirect users to when finishing the Withdraw flow.
   * @param callbackUri The URI for WePay to send IPN messages to.
   * @param note The note to be attached to the withdrawal.
   * @param withdrawalId The ID of the Withdrawal on WePay's system. This should only be populated by WePay.
   * @param state The current state of the Withdrawal.
   * @param withdrawalUri URI to view the Withdrawal on WePay's site.
   * @param recipientConfirmed Actually, I'm not entirely sure what this represents. Ooops.
   * @param createTime The time of creation, ya dig?
   * @define THIS Withdrawal
  **/
  case class Withdrawal(accountId:Long, amount:Option[Double] = None, redirectUri:Option[String] = None,
                        callbackUri:Option[String] = None, note:Option[String] = None,
                        withdrawalId:Option[Long] = None, state:Option[String] = None,
                        withdrawalUri:Option[String] = None, recipientConfirmed:Option[Boolean] = None,
                        createTime:Option[Long] = None) extends ImmutableWePayResource[Withdrawal, WithdrawalResponse] {

    val meta = Withdrawal
    val _id = withdrawalId
  }

  /**
   * Manupluates, retrieves, and searches for Withdrawal objects on WePay's system.
   *
   * @define INSTANCE Withdrawal
   * @define CRUDRESPONSETYPE WithdrawalResponse
  **/
  object Withdrawal extends ImmutableWePayResourceMeta[Withdrawal, WithdrawalResponse] {
    protected def extract(json:JValue) = json.extract[Withdrawal]
    protected def extractFindResults(json:JValue) = json.extract[List[Withdrawal]]
    protected def extractCrudResponse(json:JValue) = json.extract[WithdrawalResponse]

    /**
     * Search for Withdrawals matching the various parameters.
     *
     * @param account_id The account ID to search. Required.
     * @param state The state of the withdrawal you're interested in. Optional.
     * @param limit The maximum number of Withdrawals to return. Optional.
     * @param start The number of Withdrawals to skip in the result set. Optional.
     * @return A Box containing a List of matching Withdrawal instances.
    **/
    def find(accountId:Long, state:Option[String] = None, limit:Option[Long] = None, start:Option[Long] = None) =
      findQuery(
        ("accountId" -> accountId) ~
        ("state" -> state) ~
        ("limit" -> limit) ~
        ("start" -> start)
      )
  }
}
