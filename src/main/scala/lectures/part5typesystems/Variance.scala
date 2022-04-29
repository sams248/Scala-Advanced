package lectures.part5typesystems

object Variance extends App {

  trait Animal

  class Dog extends Animal

  class Cat extends Animal

  class Crocodile extends Animal

  // Variance is the problem of "inheritance" (type substitution) of generics

  class Cage[T] // because cat extends Animal, should a cage cat also "inherit" from cage animal?

  // Possible answers:
  // 1. yes - covariance
  class CCage[+T] // covariant cage!

  val cCage: CCage[Animal] = new CCage[Cat]

  // 2. no - invariance
  class ICage[T] // invariant cage!
  //  val iCage: ICage[Animal] = new ICage[Cat] // this is wrong, it is as if we say val x: Int = "hello"

  // 3. hell no - opposite = contravariance
  class XCage[-T]

  val xCage: XCage[Cat] = new XCage[Animal]


  class InvariantCage[T](val animal: T) // invariant

  // Covariant Positions
  class CovariantCage[+T](val animal: T) // covariant position

  // class ContravariantCage[-T](val animal: T) // would not compile: contravariant type T occurs in covariant position in type T of value animal
  /*
    val catCage: XCage[Cat] = new XCage[Animal](new Crocodile)
   */

  // class CovariantVariableCage[+T](var animal: T) // types of vars are in covariant position
  // would not compile: covariant type T occurs in contravariant position in type T of value animal_
  /*
    val cCage: CCage[Animal] = new CCage[Cat](new Cat)
    cCage.animal = new Crocodile
   */

  //  class ContravariantVariableCage[-T](var animal: T) // also in covariant position
  /*
    val catCage: XCage[Cat] = new XCage[Animal](new Crocodile)
   */
  class InvariantVariableCage[T](var animal: T) // ok

  //  trait AnotherCovariantCage[+T] {
  //    def addAnimal(animal: T) // contravariant position
  //  }
  /*
    val cCage: CCage[Animal] = new CCage[Dog]
    cCage.add(new Cat)
   */

  class AnotherContravariantCage[-T] {
    def addAnimal(animal: T) = true
  }

  val acc: AnotherContravariantCage[Cat] = new AnotherContravariantCage[Animal]
  acc.addAnimal(new Cat)

  class Kitty extends Cat

  acc.addAnimal(new Kitty)

  class MyList[+A] {
    def add[B >: A](element: B): MyList[B] = new MyList[B] // widening the type
  }

  val emptyList = new MyList[Kitty]
  val animals = emptyList.add(new Kitty)
  val moreAnimals = animals.add(new Cat)
  val evenMoreAnimals = moreAnimals.add(new Dog)

  // Method arguments are in contravariant positions

  // return types
  class PetShop[-T] {
    // def get(isItaPuppy: Boolean): T // Method return types are in contravariant position
    /*
      val catShop = new PetShop[Animal] {
        def get(isItaPuppy: Boolean): Animal = new Cat
      }

      val dogShop: PetShop[Dog] = catShop
      dogShop.get(true)   // EVIL CAT!
     */

    def get[S <: T](isItaPuppy: Boolean, defaultAnimal: S): S = defaultAnimal
  }

  val shop: PetShop[Dog] = new PetShop[Animal]

  //  val evilCat = shop.get(true, new Cat)
  class TerraNova extends Dog

  val bigFurry = shop.get(isItaPuppy = true, new TerraNova)

  /*
    Big rule:
    - method arguments are in CONTRAVARIANT position
    - return types are in COVARIANT position
   */

  // Exercise

  /**
   * API for an application that checks for illegal parking
   *
   * 1. Implement an Invariant, covariant, contravariant versions of the following API:
   * Parking[T](things: List[T]) {
   * park(vehicle: T)
   * impound(vehicles: List[T])
   * checkVehicles(conditions: String): List[T]
   * }
   *
   * 2. How the API will be different if instead of List, we used someone else's API: IList[T] (Invariant list)
   * 3. Make Parking a Monad!
   *     - only add flatMap
   */
  class Vehicle

  class Bike extends Vehicle

  class Car extends Vehicle

  class IList[T]

  class IParking[T](vehicles: List[T]) {
    def park(vehicle: T): IParking[T] = ???

    def impound(vehicles: List[T]): IParking[T] = ???

    def checkVehicles(conditions: String): List[T] = ???

    def flatMap[S](f: T => IParking[S]): IParking[S] = ???
  }

  class CParking[+T](vehicles: List[T]) {
    def park[S >: T](vehicle: S): CParking[S] = ???

    def impound[S >: T](vehicles: List[S]): CParking[S] = ???

    def checkVehicles(conditions: String): List[T] = ???

    def flatMap[S](f: T => CParking[S]): CParking[S] = ???
  }

  class CList[-T]

  class XParking[-T](vehicles: List[T]) {
    def park(vehicle: T): XParking[T] = ???

    def impound[R <: T](vehicles: CList[R]): XParking[R] = ???

    def checkVehicles[S <: T](conditions: String): List[S] = ???

    def flatMap[R <: T, S](f: R => XParking[S]): XParking[S] = ???
  }

  /*
    Rule of thumb:
    - use covariance = COLLECTION OF THINGS
    - use contravariance = GROUP OF ACTIONS
   */

  class CParking2[+T](vehicles: IList[T]) {
    def park[S >: T](vehicle: S): CParking2[S] = ???

    def impound[S >: T](vehicles: IList[S]): CParking2[S] = ???

    def checkVehicles[S >: T](conditions: String): IList[S] = ???
  }

  class XParking2[-T](vehicles: IList[T]) {
    def park(vehicle: T): XParking2[T] = ???

    def impound[S <: T](vehicles: IList[S]): XParking2[S] = ???

    def checkVehicles[S <: T](conditions: String): IList[S] = ???
  }
}
