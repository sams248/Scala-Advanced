package lectures.part4implicits

object TypeClasses extends App {

  /*
   A type class is a group of types that satisfy a contract typically defined by a trait.
   They enable us to make a function more ad-hoc polymorphic without touching its code. This flexibility is the biggest
   win with the type-class pattern.
   Type class is a trait that takes a type, and describes what operations can be applied to that type.
   */
  trait HTMLWritable {
    def toHtml: String
  }

  case class User(name: String, age: Int, email: String) extends HTMLWritable {
    override def toHtml: String = s"<div>$name ($age yo) <a href=$email/> </div>"
  }

  User("Sam", 23, "sam@gmail.com").toHtml

  /*
   1. only works for the types we write
   2. this is only one implementation (design) out of quite a number
   */

  // another option is to use pattern matching
  object HTMLSerializerPM {
    def serializeToHtml(value: Any): Unit = value match {
      case User(n, a, e) =>
      case _ =>
    }
  }

  /*
   1. we lost type safety
   2. we need to modify code every time
   3. this is still one possible implementation
   */

  trait HTMLSerializer[T] {
    def serialize(value: T): String
  }

  implicit object UserSerializer extends HTMLSerializer[User] {
    def serialize(user: User): String = s"<div>${user.name} (${user.age} yo) <a href=${user.email}/> </div>"
  }

  val john = User("John", 32, "john@gmail.com")
  println(UserSerializer.serialize(john))

  // we can now define serializers for other types

  import java.util.Date

  object DateSerializer extends HTMLSerializer[Date] {
    override def serialize(date: Date): String = s"<div>${date.toString}</div>"
  }

  // we can define multiple serializers
  object PartialUserSerializer extends HTMLSerializer[User] {
    def serialize(user: User): String = s"<div>${user.name} </div>"
  }


  // Type Classes - Part 2
  object HTMLSerializer {
    def serialize[T](value: T)(implicit serializer: HTMLSerializer[T]): String =
      serializer.serialize(value)

    def apply[T](implicit serializer: HTMLSerializer[T]): HTMLSerializer[T] = serializer
  }


  implicit object IntSerializer extends HTMLSerializer[Int] {
    override def serialize(value: Int): String = s"<div style: color=blue>$value</div>"
  }

  println(HTMLSerializer.serialize(42))
  println(HTMLSerializer.serialize(john))

  // With this design we have access to the entire type class interface
  println(HTMLSerializer[User].serialize(john))

  // Type Classes - Part 3
  implicit class HTMLEnrichment[T](value: T) {
    def toHTML(implicit serializer: HTMLSerializer[T]): String = serializer.serialize(value)
  }

  /*
   - extend to new types
   - choose implementation
   - super expressive!
   */

  println(john.toHtml) // println(new HtmlEnrichment[User](john).toHtml(UserSerializer))
  println(john.toHTML(PartialUserSerializer))
  println(2.toHTML)

  /*
    - type class itself --- HTMLSerializer[T] { .. }
    - type class instances (some of which are implicit) --- UserSerializer, IntSerializer
    - conversion with implicit classes --- HTMLEnrichment
   */

  // context bounds
  def htmlBoilerplate[T](content: T)(implicit serializer: HTMLSerializer[T]): String =
    s"<html><body> ${content.toHTML(serializer)}</body></html>"

  def htmlSugar[T: HTMLSerializer](content: T): String = {
    val serializer = implicitly[HTMLSerializer[T]]
    // use serializer
    s"<html><body> ${content.toHTML(serializer)}</body></html>"
  }

  // implicitly
  case class Permissions(mask: String)

  implicit val defaultPermissions: Permissions = Permissions("0744")

  // in some other part of the  code
  val standardPerms = implicitly[Permissions] // Summon an implicit value of type T. Usually, the argument is not passed explicitly.

}
