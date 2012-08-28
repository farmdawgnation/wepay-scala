package me.frmr.wepay.api {
  import net.liftweb.json._
    import JsonDSL._

  case class WithdrawalResponse(withdrawal_id:Long, withdrawal_uri:Option[String] = None)

  /**
   * Represents an instance of a Withdrawal.
   *
   * @param account_id The WePay Account ID associated with the Withdrawal.
   * @param amount The withdrawal amount.
   * @param redirect_uri The URI to redirect users to when finishing the Withdraw flow.
   * @param callback_uri The URI for WePay to send IPN messages to.
   * @param note The note to be attached to the withdrawal.
   * @param withdrawal_id The ID of the Withdrawal on WePay's system. This should only be populated by WePay.
   * @param state The current state of the Withdrawal.
   * @param withdrawal_uri URI to view the Withdrawal on WePay's site.
   * @param recipient_confirmed Actually, I'm not entirely sure what this represents. Ooops.
   * @param create_time The time of creation, ya dig?
   * @define THIS Withdrawal
  **/
  case class Withdrawal(account_id:Long, amount:Option[Double] = None, redirect_uri:Option[String] = None,
                        callback_uri:Option[String] = None, note:Option[String] = None,
                        withdrawal_id:Option[Long] = None, state:Option[String] = None,
                        withdrawal_uri:Option[String] = None, recipient_confirmed:Option[Boolean] = None,
                        create_time:Option[Long] = None) extends ImmutableWePayResource[Withdrawal, WithdrawalResponse] {

    val meta = Withdrawal
    val _id = withdrawal_id
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
    def find(account_id:Long, state:Option[String] = None, limit:Option[Long] = None, start:Option[Long] = None) =
      findQuery(
        ("account_id" -> account_id) ~
        ("state" -> state) ~
        ("limit" -> limit) ~
        ("start" -> start)
      )
  }
}
