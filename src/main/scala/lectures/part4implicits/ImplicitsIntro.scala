package lectures.part4implicits

// Where this feature is enabled, definitions of implicit conversions are allowed.
// If implicitConversions is not enabled, the definition of an implicit conversion will trigger a warning from the compiler.

import jdk.jfr.DataAmount

import scala.language.implicitConversions

object ImplicitsIntro extends App {

  val pair = "Daniel" -> "555" // how does it compile? there is no arrow method on String class
  val intPair = 1 -> 2 // -> is a method on an implicit class called ArrowAssoc

  /*
   Implicit classes:
   Scala 2.10 introduced a new feature called implicit classes. An implicit class is a class marked with the implicit keyword.
   This keyword makes the class’s primary constructor available for implicit conversions when the class is in scope.
   */

  case class Person(name: String) {
    def greet = s"Hi, my name is $name"
  }

  implicit def fromStringToPerson(str: String): Person = Person(str)

  println("Sam".greet) // the compiler will look for all implicit classes, objects, values and methods than can turn
  // this String into something that has a greet method! println(fromStringToPerson("Sam").greet

  // if there are multiple potential implicits that can match, the compiler will throw an error, e.g.
  //  class A {
  //    def greet: Int = 3
  //  }
  //  implicit def fromStringToA(str: String): A = new A

  /*
   Implicit parameters
   A method can have an implicit parameter list, marked by the implicit keyword at the start of the parameter list.
   If the parameters in that parameter list are not passed as usual, Scala will look if it can get an implicit value of
   the correct type, and if it can, pass it automatically.
   */

  def increment(x:Int)(implicit amount: Int) = x + amount
  implicit val defaultAmount: Int = 10
  increment(2) // NOT default args


}
