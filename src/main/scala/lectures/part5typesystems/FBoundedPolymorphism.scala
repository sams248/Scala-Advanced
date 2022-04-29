package lectures.part5typesystems

object FBoundedPolymorphism extends App {

  /*
    trait Animal {
      def breed: List[Animal]
    }

    class Cat extends Animal {
      override def breed: List[Animal] = ??? // We want a List[Cat] !!
    }

    class Dog extends Animal {
      override def breed: List[Animal] = ??? // We want a List[Dog] !!
    }

   How do I make the compiler force this type correctness?!
  */

  /*
   Solution 1 - naive (error-prone)

   trait Animal {
      def breed: List[Animal]
   }

   class Cat extends Animal {
     override def breed: List[Cat] = ???
   }

   class Dog extends Animal {
     override def breed: List[Cat] = ??? // This will also be valid code!
   }
   */


  // Solution 2 - FBP

  /*
    trait Animal[A <: Animal[A]] { // Recursive type: F-Bounded Polymorphism
      def breed: List[Animal[A]]
    }

    class Cat extends Animal[Cat] {
      override def breed: List[Animal[Cat]] = ??? // Must return List[Cat] !!
    }

    class Dog extends Animal[Dog] {
      override def breed: List[Animal[Dog]] = ??? // List[Dog] !!
    }

    // Example from ORM (Object–relational Mapping)
    trait Entity[E <: Entity[E]]

    class Person extends Comparable[Person] { // FBP
      override def compareTo(o: Person): Int = ???
    }

    class Crocodile extends Animal[Dog] { // This is wrong but compiles!
      override def breed: List[Animal[Dog]] = ???
    }
   */

  /*
   // Solution 3 - FBP + Self-types

   trait Animal[A <: Animal[A]] {
     self: A =>
     def breed: List[Animal[A]]
   }

   class Cat extends Animal[Cat] {
     override def breed: List[Animal[Cat]] = ??? // List[Cat] !!
   }

   class Dog extends Animal[Dog] {
     override def breed: List[Animal[Dog]] = ??? // List[Dog] !!
   }

   class Crocodile extends Animal[Dog] { // Compiler will complain (Illegal inheritance, self-type Crocodile does not conform to A)
     override def breed: List[Animal[Dog]] = ??? // List[Dog] !!
   }

   // limitation:
   trait Fish extends Animal[Fish]

   class Shark extends Fish {
     override def breed: List[Animal[Fish]] = List(new Cod) // wrong
   }

   class Cod extends Fish {
     override def breed: List[Animal[Fish]] = ???
   }
   */


  /*
   // Solution 4 type classes!
    trait Animal

    trait CanBreed[A] {
      def breed(a: A): List[A]
    }

    class Dog extends Animal

    object Dog {
      implicit object DogsCanBreed extends CanBreed[Dog] {
        def breed(a: Dog): List[Dog] = List()
      }
    }

    implicit class CanBreedOps[A](animal: A) {
      def breed(implicit canBreed: CanBreed[A]): List[A] =
        canBreed.breed(animal)
    }

    val dog = new Dog
    dog.breed // List[Dog]!!

    // new CanBreedOps[Dog](dog).breed(Dog.DogsCanBreed)
    // implicit value to pass to breed: Dog.DogsCanBreed

    class Cat extends Animal

    object Cat {
      implicit object CatsCanBreed extends CanBreed[Dog] {
        def breed(a: Dog): List[Dog] = List()
      }
    }

    val cat = new Cat
    cat.breed // could not find implicit value for parameter canBreed
   */


  // Solution #5

  trait Animal[A] { // pure type classes
    def breed(a: A): List[A]
  }

  class Dog

  object Dog {
    implicit object DogAnimal extends Animal[Dog] {
      override def breed(a: Dog): List[Dog] = List()
    }
  }

  class Cat

  object Cat {
    implicit object CatAnimal extends Animal[Dog] {
      override def breed(a: Dog): List[Dog] = List()
    }
  }

  implicit class AnimalOps[A](animal: A) {
    def breed(implicit animalTypeClassInstance: Animal[A]): List[A] =
      animalTypeClassInstance.breed(animal)
  }

  val dog = new Dog
  dog.breed

  val cat = new Cat
  // cat.breed // could not find implicit value for parameter animalTypeClassInstance

}
