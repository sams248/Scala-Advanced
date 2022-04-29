package exercises

import lectures.part4implicits.TypeClasses.User

class EqualityPlayground {
  /*
    Exercise: Equality
   */

  trait Equal[T] {
    def apply(a: T, b: T): Boolean
  }

  object NameEquality extends Equal[User] {
    override def apply(a: User, b: User): Boolean = a.name == b.name
  }

  object FullEquality extends Equal[User] {
    override def apply(a: User, b: User): Boolean = a.name == b.name && a.email == b.email
  }

  /*
 Exercise: implement type class pattern for the equality type class
 */

  object Equal {
    def apply[T](a: T, b: T)(implicit equalizer: Equal[T]): Boolean = equalizer.apply(a, b)
  }

  val john: User = User("John", 32, "john@gmail.com")

  val anotherJohn: User = User("John", 45, "anotherJohn@gmail.com")

  implicit object NameEquality2 extends Equal[User] {
    override def apply(a: User, b: User): Boolean = a.name == b.name
  }

  // Ad-hoc polymorphism
  println(Equal.apply(john, anotherJohn))
  println(Equal(john, anotherJohn))

  /*
   Exercise: improve the Equal TC with an implicit conversion class
   ===(anotherValue: T)
   !==(anotherValue: T)
   */
  implicit class TypeSafeEqual[T](value: T) {
    def ===(other: T)(implicit equalizer: Equal[T]): Boolean = equalizer.apply(value, other)

    def !==(other: T)(implicit equalizer: Equal[T]): Boolean = !equalizer.apply(value, other)
  }

  println(john === anotherJohn)

  /*
   Steps that the compiler take:
   john.===(anotherJohn): tries to wrap john (User) with something that has === method
   new TypeSafeEqual[User](john).===(anotherJohn)
   new TypeSafeEqual[User](john).===(anotherJohn)(NameEquality)
   */

  /*
    TYPE SAFE
   */
  // println(john == 43) // in Scala 2 this compiles and returns false (BAD, leads to bugs)
  // println(john === 43) // TYPE SAFE equality: neither Scala 2 nor Scala 3 compiles this one (best)
}
