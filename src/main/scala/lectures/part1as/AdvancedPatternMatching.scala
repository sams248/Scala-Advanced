package lectures.part1as

object AdvancedPatternMatching extends App {

  val numbers = List(1)
  val description: Unit = numbers match {
    case head :: Nil => println(s"The only element is $head")
    case _ =>
  }

  /*
    - constants
    - wildcards
    - case classes
    - tuples
    - some special magic like above
   */

  class Person(val name: String, val age: Int)

  // how to make pattern matching possible for a Class?
  object Person { // the name of the object does not have to match the name of the class as long as it match with the name in case clause
    def unapply(person: Person): Option[(String, Int)] = {
      if (person.age < 21) None
      else Some((person.name, person.age))
    }

    // overloading unapply method
    def unapply(age: Int): Option[String] = Some(if (age < 1) "minor" else "major")
  }

  val bob = new Person("Bob", 25)
  // val bob = new Person("Bob", 20) => Exception in thread "main" scala.MatchError
  val greeting = bob match {
    case Person(n, a) => s"Hi, my name is $n and I am $a years old!"
  }
  println(greeting)

  val legalStatus = bob.age match {
    case Person(status) => s"My legal status is $status"
  }
  println(legalStatus)

  /*
     Exercise: use a custom patter matching for the following
   */
  val n: Int = 44
  val mathProperty = n match {
    case x if x < 10 => "single digit"
    case x if x % 2 == 0 => "an even number"
    case _ => "no property"
  }

  // create a singleton object with unapply with the above conditions
  // convention is to use lowercase for these singletons
  object even {
    def unapply(arg: Int): Boolean = arg % 2 == 0
  }

  object singleDigit {
    def unapply(arg: Int): Boolean = arg < 10
  }

  val mathPropertyBetter = n match {
    case singleDigit() => "single digit"
    case even() => "an even number"
    case _ => "no property"
  }

  println(mathPropertyBetter)

  // infix patterns: works if you have only two things in the pattern
  case class Or[A, B](a: A, b: B) // Either

  val either = Or(2, "two")
  val humanDescription = either match {
    case number Or string => s"$number is written as $string!" // instead of case Or(number, String)
  }
  println(humanDescription)

  // decomposing sequences
  val vararg = numbers match {
    case List(1, _*) => "starting with 1" // _* is var arg pattern
  }
  println(vararg)

  // standard techniques for unapplying the whole sequence (when you don't know the number of elements in the sequence)
  // does not work in this case
  abstract class MyList[+A] {
    def head: A = ???

    def tail: MyList[A] = ???
  }

  case object Empty extends MyList[Nothing]

  case class Cons[+A](override val head: A, override val tail: MyList[A]) extends MyList[A]

  object MyList {
    def unapplySeq[A](list: MyList[A]): Option[Seq[A]] = if (list == Empty) Some(Seq.empty)
    else unapplySeq(list.tail).map(list.head +: _)
  }

  val myList: MyList[Int] = Cons(1, Cons(2, Cons(3, Empty)))
  val decomposed = myList match {
    case MyList(1, 2, _*) => "starting with 1, 2"
    case _ => "something else"
  }
  println(decomposed)

  // custom return types for unapply (the return type of unapply does not have to be an Option)
  // it can be any data structure that has these two methods: implemented: isEmpty: Boolean, get: something

  abstract class Wrapper[T] {
    def isEmpty: Boolean

    def get: T
  }

  object PersonWrapper {
    def unapply(person: Person): Wrapper[String] = new Wrapper[String] {
      def isEmpty = false

      def get: String = person.name
    }
  }

  println(bob match {
    case PersonWrapper(n) => s"This person's name is $n"
    case _ => "An alien"
  })
}
