package lectures.part4implicits

object OrganizingImplicits extends App {

  /*
   The places Scala will look for these parameters fall into two categories:
     1. Scala will first look for implicit definitions and implicit parameters that can be accessed directly
     (without a prefix) at the point the method with the implicit parameter block is called.
     2. Then it looks for members marked implicit in all the companion objects associated with the implicit candidate type.
   */

  implicit val reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  println(List(1, 4, 5, 3, 2).sorted) // sorted takes implicit ordering ord: Ordering[B]
  // if no implicit ordering is defined, compiler will find one in scala.Predef

  /*
   Implicits (used as implicit parameters):
     - var/val
     - object
     - accessor methods = defs with no parentheses
   */

  // Exercise:
  case class Person(name: String, age: Int)

  val persons = List(
    Person("Steve", 30),
    Person("Amy", 22),
    Person("John", 66)
  )

  //  object Person {
  //    implicit val alphabeticOrdering: Ordering[Person] = Ordering.fromLessThan((p1, p2) => p1.name.compareTo(p2.name) < 0)
  //  }
  //  implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((p1, p2) => p1.age.compareTo(p2.age) < 0)

  //  println(persons.sorted)

  /*
   Implicit scope
    - normal (local) scope
    - imported scope
    - companions of all types involved in method signature
        - List
        - Ordering companion object
        - all the types involved: A and any supertype
   */

  object AlphabeticNameOrdering {
    implicit val alphabeticOrdering: Ordering[Person] = Ordering.fromLessThan((p1, p2) => p1.name.compareTo(p2.name) < 0)
  }

  object AgeOrdering {
    implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((p1, p2) => p1.age.compareTo(p2.age) < 0)
  }

  import AlphabeticNameOrdering._

  println(persons.sorted)

  /*
    Exercise
      - totalPrice = most used (50%)
      - by unit count = 25%
      - by unit price = 25%
   */
  case class Purchase(nUnits: Int, unitPrice: Double)

  object Purchase {
    implicit val totalPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan((a, b) => a.nUnits * a.unitPrice < b.nUnits * b.unitPrice)
  }

  object UnitCountOrdering {
    implicit val unitCountOrdering: Ordering[Purchase] = Ordering.fromLessThan(_.nUnits < _.nUnits)
  }

  object UnitPriceOrdering {
    implicit val unitPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan(_.unitPrice < _.unitPrice)
  }
}
