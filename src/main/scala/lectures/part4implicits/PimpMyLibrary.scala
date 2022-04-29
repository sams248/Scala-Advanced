package lectures.part4implicits

import scala.annotation.tailrec
import scala.language.implicitConversions

object PimpMyLibrary extends App {
  /*
   Enrich existing types with implicits!
   This is just a fancy expression to refer to the ability to supplement a library using implicit conversions.
   In the broader context of software design patterns, this is a simplified way of achieving the Decorator Design Pattern.
   */

  // 2.isPrime, how can we achieve this? use implicit classes
  implicit class RichInt(val value: Int) extends AnyVal { // implicit classes take one and only one parameter
    def isEven: Boolean = value % 2 == 0

    def squareRoot: Double = Math.sqrt(value)

    // for exercise 2
    def times(function: () => Unit): Unit = {
      @tailrec
      def timesAux(n: Int): Unit = {
        if (n <= 0) () else {
          function()
          timesAux(n - 1)
        }
      }

      timesAux(value)
    }

    def *[T](list: List[T]): List[T] = {
      def concatenate(n: Int): List[T] = if (n <= 0) List() else {
        concatenate(n - 1) ++ list
      }

      concatenate(value)
    }
  }

  implicit class RicherInt(richInt: RichInt) {
    def isOdd: Boolean = richInt.value % 2 != 0
  }

  println(new RichInt(64).squareRoot)
  print(44.isEven) // type enrichment = pimping

  1 to 10 // to is a method in RichInt in Scala package

  import scala.concurrent.duration._

  3.seconds

  // compiler does not do multiple implicit searches: 43.isOdd will not compile

  /*
   Exercise:
   1. Enrich the string class:
    - asInt
    - encrypt: "John -> Lqjp

   2. Keep enriching the Int class
   - times(function)
   - 3.times(() => ...)
   - * : 3 * List(1,2) = List(1,2,1,2,1,2)
   */

  // 1
  implicit class RichString(string: String) {
    def asInt: Int = Integer.valueOf(string) // java.lan.Integer -> Int

    def encrypt(cypherDistance: Int): String = string.map(char => (char + cypherDistance).asInstanceOf[Char])
  }

  println("3".asInt + 4)
  println("John".encrypt(2))

  // 2
  3.times(() => println("Learning Scala"))
  println(4 * List(1, 2))

  // "3" / 4
  implicit def stringToInt(string: String): Int = Integer.valueOf(string)

  println("6" / 2) // stringToInt("6") / 2

  // equivalent to implicit class RichAltInt(value: Int)
  class RichAltInt(value: Int)

  implicit def erich(value: Int): RichAltInt = new RichAltInt(value)

  // although implicit conversions with methods are more powerful, they are discouraged
  // danger zone
  implicit def intToBoolean(i: Int): Boolean = i == 1

  /*
   if (n) do something
   else do something else
   */
  val aConditionedValue = if (3) "OK" else "Something wrong!"
  println(aConditionedValue) // if there is a bug in implicit method it is very hard to trace it back!
}
