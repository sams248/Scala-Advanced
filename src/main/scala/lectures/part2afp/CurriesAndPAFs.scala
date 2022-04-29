package lectures.part2afp

object CurriesAndPAFs extends App {

  // curried functions (functions returning other functions)
  val superAdder: Int => Int => Int = x => y => x + y

  val add3 = superAdder(3) // Int => Int : y => 3 + y
  println(add3(5))
  println(superAdder(3)(5)) // curried function

  // Scala allows definition of curried methods out of the box
  // this is a method
  def curriedAdder(x: Int)(y: Int): Int = x + y // curried method

  val add4: Int => Int = curriedAdder(4) // this does not work without type annotation!
  // val add4 = curriedAdder(4) // Missing argument list (y: Int) for method curriedAdder(Int)(Int)

  // We can't use methods in higher order functions unless they are transformed to function values
  // this is a limitation of the JVM, because methods are part of in instance, they are not instances of a function
  // function != methods

  // lifting = ETA-expansion : transforming method to function
  def inc(x: Int): Int = x + 1

  List(1, 2, 3).map(inc) // ETA-expansion:  List(1,2,3).map(x => inc(x))

  // Partial function applications
  val add5 = curriedAdder(5) _ // by adding the _ you are asking the compiler to do ETA-expansion for you

  /*
    Exercise
    implement different  implementations of add7: Int => Int = y => 7 + y using the following:
   */
  val simpleAddFunction = (x: Int, y: Int) => x + y

  def simpleAddMethod(x: Int, y: Int) = x + y

  def curriedAddMethod(x: Int)(y: Int) = x + y

  val add7 = (x: Int) => simpleAddFunction(x, 7) // simplest
  val add7_2 = simpleAddFunction.curried(7) // Creates a curried version of a function.
  val add7_3 = curriedAddMethod(7) _ // Partially applied function, _ lifts the method to function value
  val add7_4 = curriedAddMethod(7)(_) // alternative syntax
  val add7_5 = simpleAddMethod(7, _: Int) // alternative syntax for turning the method into function value
  val add7_6 = simpleAddFunction(7, _: Int)

  // underscores are powerful
  def concatenate(a: String, b: String, c: String) = a + b + c

  val insertName = concatenate("Hello, I am ", _: String, ", how are you?")
  println(insertName("Sam"))

  val fillInTheBlanks = concatenate("Hello ", _: String, _: String)
  println(fillInTheBlanks("Sam", " what's up?"))

  // Exercises
  /*
    1. Process a list of numbers and return their string representations with different formats
    Use the %4.2f, %8.6g, and %14.2f with a curried formatter function.
   */
  def curriedFormatter(s: String)(number: Double): String = s.format(number)

  val numbers = List(Math.PI, Math.E, 1, 9.8, 1.4e-12)

  val simpleFormat = curriedFormatter("%4.2f") _ // lift
  val seriousFormat = curriedFormatter("%8.6f") _
  val preciseFormat = curriedFormatter("%14.12f") _

  println(numbers.map(simpleFormat))
  println(numbers.map(seriousFormat))
  println(numbers.map(preciseFormat)) // compiler does eta-expansion for us

  /*
    2. Difference between:
        - functions vs methods
        - parameters: by-name vs 0-lambda
   */
  def byName(n: => Int) = n + 1

  def byFunction(f: () => Int) = f() + 1

  def method: Int = 88

  def parenMethod(): Int = 42

  /*
    explore calling byName and byFunction with:
    - int
    - method
    - parenMethod
    - lambda
    - PAF
    which ones (don't) compile, why?
   */

  byName(88) // ok
  byName(method) // ok
  byName(parenMethod()) // ok
  byName(parenMethod) // ok but beware ==> byName(parenMethod())
  // byName(() => 42) // not ok
  byName((() => 88) ()) // ok
  // byName(parenMethod _) // not ok
  // byFunction(88) // not ok
  // byFunction(method) // not ok, compiler does not do eta-expansion
  byFunction(parenMethod) // ok, compiler does eta-expansion
  byFunction(() => 88) // ok
  byFunction(parenMethod _) // also works, but warning _ is unnecessary
}
