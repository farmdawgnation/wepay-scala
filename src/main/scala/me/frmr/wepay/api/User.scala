package me.frmr.wepay.api {
  import net.liftweb.json._

  case class User(user_id:Long, first_name:String, last_name:String, email:String, state:String)
    extends WePayResource[User] {

    val meta = User
    val _id = user_id
  }

  object User extends WePayResourceMeta[User] {
    def extract(json:JValue) = json.extract[User]
    def extractFindResults(json:JValue) = json.extract[List[User]]
  }
}
