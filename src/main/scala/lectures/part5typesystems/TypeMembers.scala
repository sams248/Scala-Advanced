package lectures.part5typesystems

object TypeMembers extends App {
  /*
   Abstract types, such as traits and abstract classes, can in turn have abstract type members.
   This means that the concrete implementations define the actual types.
   */
  class Animal

  class Dog extends Animal

  class Cat extends Animal

  class AnimalCollection {
    type AnimalType // abstract type member
    type BoundedAnimal <: Animal
    type SuperBoundedAnimal >: Dog <: Animal
    type AnimalC = Cat // alias (used when there is name collision with other packages)
  }

  val ac = new AnimalCollection
  val dog: ac.AnimalType = ???

  //  val cat: ac.BoundedAnimal = new Cat will not compile

  val pup: ac.SuperBoundedAnimal = new Dog
  val cat: ac.AnimalC = new Cat

  type CatAlias = Cat // they also work outside
  val anotherCat: CatAlias = new Cat

  // Alternative to generics
  trait MyList {
    type T // abstract

    def add(element: T): MyList
  }

  class NonEmptyList(value: Int) extends MyList {
    override type T = Int

    def add(element: Int): MyList = ???
  }

  // .type
  type CatsType = cat.type
  val newCat: CatsType = cat
  //  new CatsType // class type required but lectures.part5typesystems.TypeMembers.cat.type found

  /*
    Exercise - Enforce a type to be applicable to SOME TYPES only
   */
  // LOCKED (we have no control over this)
  trait MList {
    type A

    def head: A

    def tail: MList
  }

  trait ApplicableToNumbers {
    type A <: Number
  }

  // NOT OK
  // with ApplicableToNumbers enforces the constraint by the compiler
  //  class CustomList(hd: String, tl: CustomList) extends MList with ApplicableToNumbers {
  //    type A = String
  //    def head = hd
  //    def tail = tl
  //  }

  // OK
  class IntList(hd: Int, tl: IntList) extends MList {
    type A = Int

    def head: Int = hd

    def tail: IntList = tl
  }

  // Number
  // type members and type member constraints (bounds)
}
