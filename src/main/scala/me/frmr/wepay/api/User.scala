package me.frmr.wepay.api {
  import net.liftweb.json._

  /**
   * A class representing a User object on WePay's server.
   *
   * These are read only, and the use of their data is restricted by the WePay Developer TOS.
   *
   * @param user_id The user's unique ID.
   * @param first_name The user's first name.
   * @param last_name The user's last name.
   * @param email The user's email.
   * @param state The state of the User's account.
  **/
  case class User(user_id:Long, first_name:String, last_name:String, email:String, state:String)
    extends WePayResource[User] {

    val meta = User
    val _id = user_id
  }

  /**
   * Meta object for retrieving Users from WePay.
   *
   * @define RESOURCE User
   * @define INSTANCE User
  **/
  object User extends WePayResourceMeta[User] {
    def extract(json:JValue) = json.extract[User]
    def extractFindResults(json:JValue) = json.extract[List[User]]
  }
}
