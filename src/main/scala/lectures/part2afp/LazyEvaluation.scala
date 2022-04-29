package lectures.part2afp

object LazyEvaluation extends App {

  // lazy delays the evaluation of values
  lazy val x: Int = throw new RuntimeException

  lazy val y: Int = {
    println("Hello!")
    88
  }
  println(y)
  // y will not be evaluated again
  println(y)

  // Examples of implications

  // example 1: side effects
  def sideEffectCondition: Boolean = {
    println("I am a side effect!")
    true
  }

  def simpleCondition: Boolean = false

  lazy val lazyCondition = sideEffectCondition
  println(if (simpleCondition && lazyCondition) "yes" else "no")

  // example 2: in conjunction with call by name
  def byNameMethod(n: => Int): Int = n + n + n + 1

  def retrieveMagicValue = {
    //side effect or long computation
    println("Waiting...")
    Thread.sleep(1000)
    44
  }

  println(byNameMethod(retrieveMagicValue))

  // use lazy vals
  def byNameMethodLazy(n: => Int): Int = {
    // call by NEED technique
    lazy val t = n // only evaluated once
    t + t + t + 1
  }

  println(byNameMethodLazy(retrieveMagicValue))

  // example 2: filtering with lazy vals
  def lessThan30(i: Int): Boolean = {
    println(s"$i is less than 30?")
    i < 30
  }

  def greaterThan20(i: Int): Boolean = {
    println(s"$i is greater than 20?")
    i > 20
  }

  val numbers = List(1, 25, 40, 5, 23)
  val lt30 = numbers.filter(lessThan30)
  val gt20lt30 = lt30.filter(greaterThan20)
  println(gt20lt30)

  val lt30Lazy = numbers.withFilter(lessThan30)
  val gt20lt30Lazy = lt30.withFilter(greaterThan20)
  println
  // evaluates predicates on a by need basis
  gt20lt30Lazy.foreach(println)

  // for-comprehensions use withFilter with guards
  for {
    a <- List(1, 2, 3, 4) if a % 2 == 0 // use lazy vals
  } yield a + 1
  // translates to:
  List(1, 2, 3, 4).withFilter(_ % 2 == 0).map(_ + 1) // List[Int]

  /*
    Exercise: implement a lazily evaluated, singly linked STREAM of elements.
    The Stream is a lazy lists where head is always available but tail is evaluated only when they are needed.

    naturals = MyStream.from(1)(x => x + 1) = stream of natural numbers (potentially infinite!)
    naturals.take(100).foreach(println) // lazily evaluated stream of the first 100 naturals (finite stream)
    naturals.foreach(println) // will crash - infinite!
    naturals.map(_ * 2) // stream of all even numbers (potentially infinite)
   */
  abstract class MyStream[+A] {
    def isEmpty: Boolean

    def head: A

    def tail: MyStream[A]

    def #::[B >: A](element: B): MyStream[B] // prepend operator

    def ++[B >: A](anotherStream: MyStream[B]): MyStream[B] // concatenate two streams

    def foreach(f: A => Unit): Unit

    def map[B](f: A => B): MyStream[B]

    def flatMap[B](f: A => MyStream[B]): MyStream[B]

    def filter(predicate: A => Boolean): MyStream[A]

    def take(n: Int): MyStream[A] // takes the first n elements out of this stream

    def takeAsList(n: Int): List[A]
  }

  object MyStream {
    def from[A](start: A)(generator: A => A): MyStream[A] = ???
  }
}
