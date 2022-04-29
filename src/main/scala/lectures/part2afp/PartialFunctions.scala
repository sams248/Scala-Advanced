package lectures.part2afp

object PartialFunctions extends App {

  val aFunction = (x: Int) => x + 1 // Function1[Int, Int] === Int => Int

  val aFussyFunction = (x: Int) => {
    if (x == 1) 44
    else if (x == 2) 55
    else if (x == 5) 66
    else throw new FunctionNotApplicableException
  }

  class FunctionNotApplicableException extends RuntimeException

  val aNicerFussyFunction = (x: Int) => x match {
    case 1 => 44
    case 2 => 55
    case 5 => 66
  } // a proper function
  // {1, 2, 5} => Int

  val aPartialFunction: PartialFunction[Int, Int] = {
    case 1 => 44
    case 2 => 55
    case 5 => 66
  } // partial function value

  println(aPartialFunction(2))
  // println(aPartialFunction(8)) // Exception in thread "main" scala.MatchError, because partial functions are based on pattern matching

  // PF utilities

  println(aPartialFunction.isDefinedAt(2))

  // lift PT to total functions that return options
  val lifted = aPartialFunction.lift // Int => Option[Int]
  println(lifted(2))
  println(lifted(88))

  val pfChain = aPartialFunction.orElse[Int, Int] {
    case 7 => 77
  }
  println(pfChain(2))
  println(pfChain(7))

  // PFs extend normal functions
  val aTotalFunction: Int => Int = {
    case 1 => 88
  }
  // therefore HOFs accept partial functions
  val aMappedList = List(1, 2, 3).map {
    case 1 => 42
    case 2 => 43
    case 3 => 44
  }
  println(aMappedList)

  /*
   Note: PFs can only have one parameter type
   */

  /**
   * Exercise
   * 1. construct a PF instance yourself (anonymous class
   * 2. dumb chat-bot as a PF
   */

  // 1
  val aManualFussyFunction = new PartialFunction[Int, Int] {
    override def apply(x: Int): Int = x match {
      case 1 => 44
      case 2 => 55
      case 5 => 66
    }

    override def isDefinedAt(x: Int): Boolean = x == 1 || x == 2 || x == 5
  }

  // 2
  val chatbot: PartialFunction[String, String] = {
    case "hello" => "Hi there!"
    case "goodbye" => "I hate to say goodbye!"
    case "call mom" => "Unable to find your phone without your credit card!"
  }

  scala.io.Source.stdin.getLines().map(chatbot).foreach(println)
}
