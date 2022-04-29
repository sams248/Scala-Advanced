package lectures.part5typesystems

object SelfTypes extends App {

  /*
   Self-types are a way to declare that a trait must be mixed into another trait, even though it does’t directly extend it.
   That makes the members of the dependency available without imports.
   */

  trait Instrumentalist {
    def play(): Unit
  }

  trait Singer {
    self: Instrumentalist => // SELF TYPE whoever implements Singer to implement Instrumentalist (this: Instrumentalist =>)
    // it say whoever extends the singer trait should also extend the Instrumentalist trait

    // rest of the implementation or API
    def sing(): Unit
  }

  class LeadSinger extends Singer with Instrumentalist {
    override def play(): Unit = ???

    override def sing(): Unit = ???
  }

  // This is illegal:
  //  class Vocalist extends Singer {
  //    override def sing(): Unit = ???
  //  }

  val jamesHetfield = new Singer with Instrumentalist {
    override def play(): Unit = ???

    override def sing(): Unit = ???
  }

  class Guitarist extends Instrumentalist {
    override def play(): Unit = println("(guitar solo)")
  }

  val ericClapton = new Guitarist with Singer {
    override def sing(): Unit = ???
  }

  // Self-types vs Inheritance
  class A

  class B extends A // B IS AN A

  trait T

  trait S {
    self: T =>
  } // S REQUIRES a T

  // Self type are used in CAKE PATTERN => "dependency injection" (in Java)
  class Component {
    // Some API
  }

  // Classical dependency injection
  class ComponentA extends Component

  class ComponentB extends Component

  class DependentComponent(val component: Component)

  // Cake pattern
  trait ScalaComponent {
    // API
    def action(x: Int): String
  }

  trait ScalaDependentComponent {
    self: ScalaComponent =>
    def dependentAction(x: Int): String = action(x) + " this rocks!"
  }

  trait ScalaApplication {
    self: ScalaDependentComponent with ScalaComponent =>} // Scala 3 specifically needs to add the ScalaComponent requirement

  // layer 1 - small components
  trait Picture extends ScalaComponent

  trait Stats extends ScalaComponent

  // layer 2 - compose
  trait Profile extends ScalaDependentComponent with Picture

  trait Analytics extends ScalaDependentComponent with Stats

  // layer 3 - app
  trait AnalyticsApp extends ScalaApplication with Analytics

  // Cyclical dependencies

  //  class X extends Y
  //  class Y extends X

  trait X {
    self: Y =>}

  trait Y {
    self: X =>}
}