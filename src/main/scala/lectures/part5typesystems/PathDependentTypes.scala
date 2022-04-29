package lectures.part5typesystems


object PathDependentTypes extends App {

  /*
   Scala has a strong type system that allows us to write code with more restrictions and checks at compile time.
   By encoding logic in the type system, we can detect errors at compile-time without introducing them to the runtime.
   One feature, in particular, is a kind of dependent typing called path-dependent types.
   A dependent type is a type, whose definition depends on a value.
   A path-dependent type is a specific kind of dependent type, where the dependent-upon value is a path.
   */

  // Path-dependent types

  class Outer {
    class Inner

    object InnerObject

    type InnerType

    def print(i: Inner): Unit = println(i)

    def printGeneral(i: Outer#Inner): Unit = println(i)
  }

  def aMethod: Int = {
    class HelperClass
    type HelperType = String // Compiler requires an alias (String)
    2
  }

  // In the case of types nested inside classes (inner classes and inner objects), class/object/trait members are defined per-instance
  val o = new Outer
  // val inner = new Inner // will not compile
  // val inner = new Outer.Inner // will not compile
  val inner = new o.Inner // o.Inner is it's own TYPE

  val oo = new Outer
  //  val otherInner: oo.Inner = new o.Inner // will not compile because o.inner and oo.inner are different types

  o.print(inner)
  // oo.print(inner) will not compile

  // All the inner types have a common super type: Outer#Inner
  o.printGeneral(inner) // inner is a subtype of general type Outer#Inner
  oo.printGeneral(inner)

  /*
    Exercise:
    Suppose you are the developer of a small DB, keyed by Int or String, but maybe (extendable in future by) other types
    Hint: Use path-dependent types and abstract type members and/or type aliases
   */

  // mixing with this trait creates a type constraint
  trait ItemLike {
    type Key
  }

  trait Item[K] extends ItemLike {
    type Key = K // type constraint
  }

  trait IntItem extends Item[Int]

  trait StringItem extends Item[String]

  def get[ItemType <: ItemLike](key: ItemType#Key): ItemType = ???

  get[IntItem](42) // ok
  get[StringItem]("home") // ok
  // get[IntItem]("scala") // not ok, we should be able to prevent this to make out API type safe

  /*
    Note: this exercise (and its solution) is only applicable to Scala 2.
    Scala 3 considers abstract path-dependent types (aka general type projection) to be unsound:
    https://docs.scala-lang.org/scala3/reference/dropped-features/type-projection.html
  */
}