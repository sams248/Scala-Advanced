package lectures.part3concurrency

/*
 The ExecutionContext is used to configure how and on which thread pools asynchronous tasks (such as Futures) will run,
 so the specific ExecutionContext that is selected is important.
 If your application does not define an ExecutionContext elsewhere, consider using Scala's global ExecutionContext.
 Execution context handles thread allocation of Futures, very rarely you have to create it yourself
 */

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Random, Success, Try}

object FutureAndPromises extends App {

  /*
    Futures are functional ways of computing something in parallel.
    Future represents a result of an asynchronous computation that may or may not be available yet. When we create a new
    Future, Scala spawns a new thread and executes its code. Once the execution is finished, the result of the
    computation (value or exception) will be assigned to the Future.
   */
  def calculateTheMeaningOfLife: Int = {
    Thread.sleep(2000)
    88
  }

  val aFuture = Future {
    calculateTheMeaningOfLife
  } // (global) is passed by the compiler

  println(aFuture.value) // Option[Try[Int]], an option because the Future might not have finished at this point

  // a Future is a computation that will hold a value, which is computed by some thread at some point in time
  println("Waiting on the Future...")

  // When this future is completed, either through an exception, or a value, apply the provided function.
  // If the future has already been completed, this will either be applied immediately or be scheduled asynchronously.
  aFuture.onComplete {
    case Success(meaningOfLife) => println(s"The meaning of life is $meaningOfLife")
    case Failure(e) => println(s"I have failed with $e")
  } // will be called by SOME thread

  Thread.sleep(3000) // gives the future time to complete

  // Example: Mini Social Network
  case class Profile(id: String, name: String) {
    def poke(anotherProfile: Profile): Unit = {
      println(s"${this.name} is poking ${anotherProfile.name}!")
    }
  }

  object SocialNetwork {
    // Database of users
    val names = Map(
      "fb.id.1-zuck" -> "Mark",
      "fb.id.1-bill" -> "Bill",
      "fb.id.1-dummy" -> "Dummy"
    )

    // Map of friendships
    val friends = Map(
      "fb.id.1-zuck" -> "fb.id.1-bill"
    )

    val random = new Random()

    def fetchProfile(id: String): Future[Profile] = Future {
      // simulate fetching from the database
      Thread.sleep(random.nextInt(300))
      Profile(id, names(id))
    }

    def fetchBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(random.nextInt(400))
      val bfId = friends(profile.id)
      Profile(bfId, names(bfId))
    }
  }

  // client application: mark to poke bill (ugly version-nesting of callbacks)
  // first, fetch mark's profile, then fetch bill's profile. mark pokes bill!
  //  val mark: Future[Profile] = SocialNetwork.fetchProfile(id = "fb.id.1-zuck")
  //  mark.onComplete {
  //    case Success(markProfile) =>
  //      val bill = SocialNetwork.fetchBestFriend(markProfile)
  //      bill.onComplete {
  //        case Success(billProfile) => markProfile.poke(billProfile)
  //        case Failure(e) => e.printStackTrace()
  //      }
  //    case Failure(exception) => exception.printStackTrace()
  //  }
  //
  //  // To make sure the Futures have enough time to finish
  //  Thread.sleep(1000)

  // functional composition of futures
  // map, flatMap, filter
  val mark: Future[Profile] = SocialNetwork.fetchProfile(id = "fb.id.1-zuck")
  val nameOnTheWall = mark.map(profile => profile.name) // if the original Future fails with an exception, mapped Future will fail with the same exception

  val marksBestFriend = mark.flatMap(profile => SocialNetwork.fetchBestFriend(profile))

  val zucksBestFriendRestricted = marksBestFriend.filter(profile => profile.name.startsWith("Z"))

  // for-comprehension (since we already have map, flatMap, and filter)
  for {
    mark <- SocialNetwork.fetchProfile(id = "fb.id.1-zuck")
    bill <- SocialNetwork.fetchBestFriend(mark)
  } mark.poke(bill)

  Thread.sleep(1000)

  // fallbacks
  // recover creates a new future that will handle any matching throwable that this future might contain.
  // If there is no match, or if this future contains a valid result then the new future will contain the same.
  val aProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recover {
    case e: Throwable => Profile("fb.id.0-dummy", "Forever alone")
  }

  // recoverWith creates a new future that will handle any matching throwable that this future might contain by assigning it a value of another future.
  // If there is no match, or if this future contains a valid result then the new future will contain the same result.
  val aFetchedProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recoverWith {
    case e: Throwable => SocialNetwork.fetchProfile("fb.id.0-dummy")
  }

  // fallbackTo creates a new future which holds the result of this future if it was completed successfully, or, if not,
  // the result of the that future if that is completed successfully.
  // If both futures are failed, the resulting future holds the throwable object of the first future.
  // Using this method will not cause concurrent programs to become nondeterministic.
  val fallbackResult = SocialNetwork.fetchProfile("unknown id").fallbackTo(SocialNetwork.fetchProfile("fb.id.0-dummy"))


  // Online Banking App
  case class User(name: String)

  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    val name = "Sam's Banking App"

    // API
    def fetchUser(name: String): Future[User] = Future {
      // simulate fetching from the DB
      Thread.sleep(500)
      User(name)
    }

    def createTransaction(user: User, merchantName: String, amount: Double): Future[Transaction] = Future {
      // simulate some processes
      Thread.sleep(1000)
      Transaction(user.name, merchantName, amount, "Success")
    }

    def purchase(username: String, item: String, merchantName: String, cost: Double): String = {
      println(s"Purchasing item: $item...")
      // fetch the user from the DB
      // create a transaction
      // WAIT for the transaction to finish
      val transactionStatusFuture = for {
        user <- fetchUser(username)
        transaction <- createTransaction(user, merchantName, cost)
      } yield transaction.status

      // Await and return the result (of type T) of an Awaitable.
      // Blocks until the Future is complete
      // atMost – maximum wait time, which may be negative (no waiting is done), Duration.Inf for unbounded waiting, or a finite positive duration
      Await.result(transactionStatusFuture, atMost = 2.seconds) // implicit conversion -> pimp my library technique!
    }
  }

  println(BankingApp.purchase("Sam", "Phone", "Saul Goodman Phone Store", cost = 3000))

  // Promises (to complete a Future at the time of our choosing)
  val promise = Promise[Int]() // "controller" over a future
  val future = promise.future // future is under the management of the promise

  // Small producer-consumer using futures and promises

  // thread 1 - "consumer" (knows how to handle future's completion)
  future.onComplete {
    case Success(r) => println("[consumer] I received " + r)
  }

  // thread 2 - "producer"
  val producer = new Thread(() => {
    println("[producer] crunching numbers...")
    Thread.sleep(1000)
    // "fulfilling" the promise, completes the promise with a value.
    promise.success(88) // or a failure!
    println("[producer] done")
  })

  producer.start()
  Thread.sleep(1000)


  /*
   1. fulfill a future immediately with a value
   2. inSequence(fa, fb)
   3. first(fa, fb) => new future with the first value of the two futures
   4. last(fa, fb) => new future with the last value of the two futures
   5. retryUntil[T](action: () => Future[T], condition: T => Boolean): Future[T]
   */

  // 1
  def fulfillImmediately[T](value: T): Future[T] = Future(value)

  // 2
  def inSequence[A, B](first: Future[A], second: Future[B]): Future[B] = {
    first.flatMap(_ => second)
  }

  // 3
  def first[A](fa: Future[A], fb: Future[A]): Future[A] = {
    val promise = Promise[A]

    def tryComplete(promise: Promise[A], result: Try[A]): Unit = result match {
      case Success(r) => try {
        promise.success(r)
      } catch {
        case _ =>
      }
      case Failure(t) => try {
        promise.failure(t)
      } catch {
        case _ =>
      }
    }

    fa.onComplete(tryComplete(promise, _)) // this can be simplified using an existing method available on promise: fa.onComplete(promise.tryComplete)

    fb.onComplete(promise.tryComplete) // promise.tryComplete tries to complete the promise with either a value or the exception.
    promise.future
  }

  // 4
  def last[A](fa: Future[A], fb: Future[A]): Future[A] = {
    // use 2 promises:
    // 1-promise which both futures will try to complete
    // 2-promise which the last future will complete
    val bothPromise = Promise[A]
    val lastPromise = Promise[A]

    val checkAndComplete = (result: Try[A]) => {
      if (!bothPromise.tryComplete(result)) lastPromise.complete(result)
    }

    fa.onComplete(checkAndComplete)

    fb.onComplete(checkAndComplete)

    lastPromise.future
  }

  val fast = Future {
    Thread.sleep(100)
    44
  }

  val slow = Future {
    Thread.sleep(200)
    88
  }

  first(fast, slow).foreach(f => println("First: " + f))
  last(fast, slow).foreach(l => println("Last: " + l))

  Thread.sleep(1000)

  // retry until
  def retryUntil[A](action: () => Future[A], condition: A => Boolean): Future[A] = {
    action()
      .filter(condition).recoverWith {
      case _ => retryUntil(action, condition)
    }
  }

  val random = new Random()
  val action = () => Future {
    Thread.sleep(100)
    val nextValue = random.nextInt(100)
    println("Generated " + nextValue)
    nextValue
  }

  retryUntil(action, (x: Int) => x < 50).foreach(result => println("Settled at " + result))
  Thread.sleep(10000)
}
