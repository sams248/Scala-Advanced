package lectures.part4implicits

import java.util.Date

object JSONSerialization extends App {

  /*
    Social Network Example:
    Users, Posts, Feeds data structures
    Serialize to JSON to pass to front-end or between multiple services
   */

  case class User(name: String, age: Int, email: String)

  case class Post(content: String, createdAt: Date)

  case class Feed(user: User, posts: List[Post]) // user is the owner of the feed

  /*
   Steps we take to use type class pattern:
    1 - Create some intermediate data types that could be stringified as JSON from primitive data types: Int, String, List, Date
    2 - Create type classes for conversion from our case classes to our intermediate data types
    3 - Serialize those intermediate data types to JSON
   */

  // Step 1
  sealed trait JSONValue { // intermediate data type representation
    def stringify: String
  }

  final case class JSONString(value: String) extends JSONValue {
    def stringify: String =
      "\"" + value + "\""
  }

  final case class JSONNumber(value: Int) extends JSONValue {
    def stringify: String = value.toString
  }

  final case class JSONArray(values: List[JSONValue]) extends JSONValue {
    def stringify: String = values.map(_.stringify).mkString("[", ",", "]")
  }

  final case class JSONObject(values: Map[String, JSONValue]) extends JSONValue {
    /*
    Intermediate representation for an object like:
      {
        name: "John"
        age: 28
        friends: [ ... ]
        latestPost: {
          content: "Learning Scala!"
          date: ...
        }
      }
     */
    def stringify: String = values.map {
      case (key, value) => "\"" + key + "\":" + value.stringify
    }
      .mkString("{", ",", "}")
  }

  val data = JSONObject(Map(
    "user" -> JSONString("Sam"),
    "posts" -> JSONArray(List(
      JSONString("Learning Scala!"),
      JSONNumber(2022)
    ))
  ))

  println(data.stringify)


  // Step 2
  /*
   Three fundamental things that we need for type classes:
    1 - Type class itself
    2 - Type class instances (implicit)
    3 - A method to use Type class instances: pimp library (conversion)
   */

  // 2.1
  trait JSONConverter[T] {
    def convert(value: T): JSONValue
  }

  // 2.3 conversion

  implicit class JSONOps[T](value: T) {
    def toJSON(implicit converter: JSONConverter[T]): JSONValue =
      converter.convert(value)
  }

  // 2.2
  // Existing data types
  implicit object StringConverter extends JSONConverter[String] {
    def convert(value: String): JSONValue = JSONString(value)
  }

  implicit object NumberConverter extends JSONConverter[Int] {
    def convert(value: Int): JSONValue = JSONNumber(value)
  }

  // Custom data types
  implicit object UserConverter extends JSONConverter[User] {
    def convert(user: User): JSONValue = JSONObject(Map(
      "name" -> JSONString(user.name),
      "age" -> JSONNumber(user.age),
      "email" -> JSONString(user.email)
    ))
  }

  implicit object PostConverter extends JSONConverter[Post] {
    def convert(post: Post): JSONValue = JSONObject(Map(
      "content" -> JSONString(post.content),
      "created:" -> JSONString(post.createdAt.toString)
    ))
  }

  implicit object FeedConverter extends JSONConverter[Feed] {
    def convert(feed: Feed): JSONValue = JSONObject(Map(
      "user" -> feed.user.toJSON,
      "posts" -> JSONArray(feed.posts.map(_.toJSON))
    ))
  }

  // Step 3: Call stringify on result
  val now = new Date(System.currentTimeMillis())
  val john = User("Sam", 28, "sam@learnscala.com")
  val feed = Feed(john, List(
    Post("Hello", now),
    Post("Look at this cute monkey!", now)
  ))

  println(feed.toJSON.stringify)
}