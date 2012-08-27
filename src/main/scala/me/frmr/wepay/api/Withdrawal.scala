package me.frmr.wepay.api {
  import net.liftweb.json._
    import JsonDSL._

  case class WithdrawalResponse(withdrawal_id:Long, withdrawal_uri:Option[String] = None)

  case class Withdrawal(account_id:Long, amount:Option[Double] = None, redirect_uri:Option[String] = None,
                        callback_uri:Option[String] = None, note:Option[String] = None,
                        withdrawal_id:Option[Long] = None, state:Option[String] = None,
                        withdrawal_uri:Option[String] = None, recipient_confirmed:Option[Boolean] = None,
                        create_time:Option[Long] = None) extends ImmutableWePayResource[Withdrawal, WithdrawalResponse] {

    val meta = Withdrawal
    val _id = withdrawal_id
  }

  /**
   * @inheritdoc
   *
   * @param RESOURCE Withdrawal
   * @param CRUDRESPONSETYPE WithdrawalResponse
  **/
  object Withdrawal extends ImmutableWePayResourceMeta[Withdrawal, WithdrawalResponse] {
    def extract(json:JValue) = json.extract[Withdrawal]
    def extractFindResults(json:JValue) = json.extract[List[Withdrawal]]
    def extractCrudResponse(json:JValue) = json.extract[WithdrawalResponse]

    def find(account_id:Long, state:Option[String] = None, limit:Option[Long] = None, start:Option[Long] = None) =
      findQuery(
        ("account_id" -> account_id) ~
        ("state" -> state) ~
        ("limit" -> limit) ~
        ("start" -> start)
      )
  }
}
