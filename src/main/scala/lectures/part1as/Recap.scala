package lectures.part1as

import scala.annotation.tailrec

object Recap extends App {

  val aCondition: Boolean = false
  val aConditionVal = if (aCondition) 42 else 88

  // instructions vs expressions

  // compiler infers types for us

  val aCodeBlock = {
    if (aCondition) 55
    56
  }

  // Unit = void in other languages
  val theUnit: Unit = println("Hello, Scala!")

  // functions
  def aFunction(x: Int): Int = x + 1

  // recursion: stack and tail
  @tailrec
  def factorial(n: Int, accumulator: Int): Int = {
    if (n <= 0) accumulator
    else factorial(n - 1, n * accumulator)
  }

  //object-oriented programming
  class Animal

  class Dog extends Animal

  val aDog: Animal = new Dog // subtyping polymorphism

  trait Carnivore {
    def eat(a: Animal): Unit
  }

  class Crocodile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("crunch!")
  }

  // method notations
  val aCrocodile = new Crocodile
  aCrocodile.eat(aDog)
  aCrocodile eat aDog // similar to natural language

  println(1.+(2))

  // anonymous classes
  val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = println("roar!")
  }

  // generics
  abstract class MyList[+A] // variance and variance problems!

  // singleton objects and companions
  object MyList

  // case classes
  case class Person(name: String, age: Int)

  // exceptions and try/catch/finally
  // val throwsException = throw new RuntimeException // Nothing
  val aPotentialFailure = try {
    throw new RuntimeException
  } catch {
    case e: Exception => "I caught an exception"
  } finally {
    print("some logs")
  }

  // functional programming
  val incrementer = new Function[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }
  incrementer(1)

  val anonymousIncrementer = (x: Int) => x + 1

  List(1, 2, 3).map(anonymousIncrementer) // higher order function

  // map, flatMap, filter
  // for-comprehension
  val pairs = for {
    num <- List(1, 2, 3) // a guard can be added using if condition
    char <- List('a', 'b', 'c')
  } yield num + "_" + char

  // Scala collections: Seqs, Arrays, Lists, Vectors, Maps, Tuples
  val aMap = Map("Sam" -> 41, "Jess" -> 45)

  // "collections": Options, Try
  val anOption = Some(2)

  // pattern matching
  val x = 2
  val order = x match {
    case 1 => "first"
    case 2 => "second"
    case 3 => "third"
    case _ => x + "th"
  }

  val bob = Person("bob", 22)
  val greeting = bob match {
    case Person(n, _) => s"Hi, my name is $n!"
  }

  // all the patterns!
}
