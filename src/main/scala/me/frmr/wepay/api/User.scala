package me.frmr.wepay.api {
  import net.liftweb.json._

  import me.frmr.wepay._

  /**
   * A class representing a User object on WePay's server.
   *
   * These are read only, and the use of their data is restricted by the WePay Developer TOS.
   *
   * @param userId The user's unique ID.
   * @param userName The full name of the user.
   * @param firstName The user's first name.
   * @param lastName The user's last name.
   * @param email The user's email.
   * @param state Either "registered" if the user has registered, or "pending" if the user still needs to confirm their registration.
  **/
  case class User(
    userId: Long,
    userName: String,
    firstName: String,
    lastName: String,
    email: String,
    state: String
  ) extends WePayResource[User] {
    val meta = User
    val _id = userId
  }

  /**
   * Meta object for retrieving Users from WePay.
   *
   * @define RESOURCE User
   * @define INSTANCE User
  **/
  object User extends WePayResourceMeta[User] {
    protected def extract(json:JValue) = json.extract[User]
    protected def extractFindResults(json:JValue) = json.extract[List[User]]

    /**
     * Retrieve the current user based on the authorization token provided.
    **/
    def apply()(implicit authorizationToken:Option[WePayToken]) = query(None)
  }
}
