package lectures.part5typesystems

object AdvancedInheritance extends App {

  // An API for I/O library
  trait Writer[T] {
    def write(value: T): Unit
  }

  trait Closeable {
    def close(status: Int): Unit
  }

  // things that we read from resources
  trait GenericStream[T] {
    // some methods
    def foreach(f: T => Unit): Unit
  }

  // GenericStream[T] with Writer[T] with Closeable is it's own type, and will have access to all API methods
  def processStream[T](stream: GenericStream[T] with Writer[T] with Closeable): Unit = {
    stream.foreach(println)
    stream.close(0)
  }


  // The diamond problem
  /*
   The "diamond problem" is an ambiguity that arises when two classes B and C inherit from A, and class D inherits
   from both B and C. If there is a method in A that B and C have overridden, and D does not override it, then
   which version of the method does D inherit: that of B, or that of C?
   */
  trait Animal {
    def name: String
  }

  trait Lion extends Animal {
    override def name: String = "lion"
  }

  trait Tiger extends Animal {
    override def name: String = "tiger"
  }

  class Mutant extends Lion with Tiger

  val m = new Mutant
  println(m.name)
  /*
    Mutant
    extends Animal with { override def name: String = "lion" }
    with { override def name: String = "tiger" }
    Last override gets picked
   */


  // The super problem + type linearization
  /*
   In Scala, trait linearization is a property that helps to rectify ambiguity when instances of a class that are
   defined using multiple inheritances from different classes and traits are created.
   It resolves ambiguity that may arise when class or trait inherits property from 2 different parents (they may be classes or traits).
   */
  trait Cold {
    def print(): Unit = println("cold")
  }

  trait Green extends Cold {
    override def print(): Unit = {
      println("green")
      super.print()
    }
  }

  trait Blue extends Cold {
    override def print(): Unit = {
      println("blue")
      super.print()
    }
  }

  class Red {
    def print(): Unit = println("red")
  }

  class White extends Red with Green with Blue {
    override def print(): Unit = {
      println("white")
      super.print()
    }
  }

  val color = new White
  color.print()
}
