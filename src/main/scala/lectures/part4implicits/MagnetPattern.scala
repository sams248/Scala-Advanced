package lectures.part4implicits

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global


object MagnetPattern extends App {

  // Magnet Pattern: A use case of Type classes that aims at solving some of the problems created by method overloading

  // Example: An API for remote peer to peer communication protocol. You should be able to handle various type of messages

  class P2PRequest

  class P2PResponse

  class Serializer[T]

  trait Actor {
    def receive(statusCode: Int): Int

    def receive(request: P2PRequest): Int

    def receive(response: P2PResponse): Int

    def receive[T: Serializer](message: T): Int // def receive[T](message: T)(implicit serializer: Serializer[T]): Int

    def receive[T: Serializer](message: T, statusCode: Int): Int

    def receive(future: Future[P2PRequest]): Int

    // lots of overloads
  }

  /*
   This poses a number of problems:
    1 - Type erasure: could not add def receive(future: Future[P2PResponse]): Int
    2 - Lifting doesn't work for all overloads
        val receiveFV = receive _ // what does _ mean here?! compiler will be confused
    3 - Code duplication
    4 - Type inference and default args: actor.receive(), what is the default?
   */

  // We can re-write this API:

  trait MessageMagnet[Result] {
    def apply(): Result
  }

  def receive[R](magnet: MessageMagnet[R]): R = magnet() // magnet.apply()

  // How can we make sure, that this receive method can get other types as arguments? by implicits conversions

  implicit class FromP2PRequest(request: P2PRequest) extends MessageMagnet[Int] {
    def apply(): Int = {
      // logic for handling a P2PRequest
      println("Handling P2P request!")
      88
    }
  }

  implicit class FromP2PResponse(response: P2PResponse) extends MessageMagnet[Int] {
    def apply(): Int = {
      // logic for handling a P2PResponse
      println("Handling P2P response!")
      44
    }
  }

  receive(new P2PRequest)
  receive(new P2PResponse)

  // Benefits:
  // 1 - No more type erasure problems!

  implicit class FromRequestFuture(future: Future[P2PRequest]) extends MessageMagnet[Int] {
    override def apply(): Int = 888
  }

  implicit class FromResponseFuture(future: Future[P2PResponse]) extends MessageMagnet[Int] {
    override def apply(): Int = 444
  }

  println(receive(Future(new P2PRequest)))
  println(receive(Future(new P2PResponse)))

  // 2 - Lifting works
  trait MathLib { // note that it does not have any parameters!
    def add1(x: Int): Int = x + 1

    def add1(s: String): Int = s.toInt + 1
    // add1 overloads
  }

  // Wre-write as a "magnetize" API
  trait AddMagnet {
    def apply(): Int
  }

  def add1(magnet: AddMagnet): Int = magnet()

  implicit class AddInt(x: Int) extends AddMagnet {
    override def apply(): Int = x + 1
  }

  implicit class AddString(s: String) extends AddMagnet {
    override def apply(): Int = s.toInt + 1
  }

  val addFV = add1 _
  println(addFV(1))
  println(addFV("3"))

  //  val receiveFV = receive _
  //  receiveFV(new P2PResponse)

  /*
    Drawbacks
      1 - Verbose
      2 - Harder to read
      3 - You can't name or place default arguments
      4 - Call by name doesn't work correctly (exercise: prove it!) (hint: think of side effects)
   */

  class Handler {
    def handle(s: => String): Unit = {
      println(s)
      println(s)
    }
    // other overloads
  }

  trait HandleMagnet {
    def apply(): Unit
  }

  def handle(magnet: HandleMagnet): Unit = magnet()

  implicit class StringHandle(s: => String) extends HandleMagnet {
    override def apply(): Unit = {
      println(s)
      println(s)
    }
  }

  def sideEffectMethod(): String = {
    println("Hello, Scala!")
    "hahaha"
  }

  // handle(sideEffectMethod())
  handle {
    println("Hello, Scala!") // will be printed once only
    "hahaha" // new StringHandle("hahaha")
  }
  // careful!

}
