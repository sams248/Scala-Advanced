package lectures.part3concurrency

import scala.collection.mutable
import scala.util.Random

object ThreadCommunication extends App {

  /*
    the producer-consumer problem

    producer -> [ ? ] -> consumer
   */
  class SimpleContainer {
    private var value: Int = 0

    def isEmpty: Boolean = value == 0

    def set(newValue: Int): Unit = value = newValue

    def get: Int = {
      val result = value
      value = 0
      result
    }
  }

  def naiveProdCons(): Unit = {
    val container = new SimpleContainer
    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      while (container.isEmpty) {
        println("[consumer] actively waiting...")
      }
      println("[consumer] I have consumed " + container.get)
    })


    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(500)
      val value = 99
      println("[producer] I have produced, after ling work, the value " + value)
      container.set(value)
    })

    /*
      This test assumes the consumer starts first and waits for the producer.
      It may be the case that the producer may start and finish first, and the consumer is stuck waiting.
      This simple implementation does now include that case!
     */
    consumer.start()
    producer.start()
  }

  // naiveProdCons()

  // wait and notify
  def smartProdCond(): Unit = {
    val container = new SimpleContainer
    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      container.synchronized { // lock the object's monitor
        container.wait() // waiting on an object's monitor, releases the lock and suspends the thread indefinitely
        // when allowed to proceed, locks the monitor again and continues
      }

      // container must have some value (only producer can wake up from waiting)
      println("[consumer] I have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("[producer] Hard at work...")
      Thread.sleep(2000)
      val value = 88

      // monitor is a data structure that is internally used by JVM to keep track of which object is locked by which thread
      container.synchronized { // this will lock the object's monitor, and any other thread trying to run the following code will block
        println("[producer] I'm producing " + value)
        container.set(value)
        container.notify() // gives signal to ONE of the sleeping threads that are waiting on this object's monitor that
        // they may continue after the acquire the lock on monitor again. which ONE thread? you don't know!
      } // until the code is evaluated and the lock is released and monitor is unlocked
    })

    consumer.start()
    producer.start()
  }

  //smartProdCond()

  /*
    extending producer-consumer problem to many values
    producer -> [ ? ? ? ] -> consumer
    both producer and consumer may block each other
   */

  def prodConsLargeBuffer(): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 3
    val consumer = new Thread(() => {
      val random = new Random()
      while (true) {
        buffer.synchronized {
          if (buffer.isEmpty) {
            println("[consumer] buffer empty, waiting...")
            buffer.wait()
          }
          // there must be at list ONE value in the buffer
          val x = buffer.dequeue()
          println("[consumer] consumed " + x)

          // hey producer! there is empty space available!
          buffer.notify()
        }
        Thread.sleep(random.nextInt(500))
      }
    })

    val producer = new Thread(() => {
      val random = new Random()
      var i = 0
      while (true) {
        buffer.synchronized {
          if (buffer.size == capacity) {
            println("[producer] buffer is full, waiting...")
            buffer.wait()
          }

          // there must be at least ONE empty space in the buffer
          println("[producer] producing " + i)
          buffer.enqueue(i)

          // hey consumer, new food for you!
          buffer.notify()
          i += 1
        }
        Thread.sleep(random.nextInt(250))
      }
    })

    consumer.start()
    producer.start()
  }

  // prodConsLargeBuffer()

  /*
    Producer-consumer, multi producers & multi-consumers
      producer1 -> [ ? ? ? ] -> consumer1
      producer2 ----^     ^---- consumer2
   */

  class Consumer(id: Int, buffer: mutable.Queue[Int]) extends Thread {
    override def run(): Unit = {
      val random = new Random()
      while (true) {
        buffer.synchronized {
          /*
           Consider the following scenario, which explains why if should be replaced by while in this case:
            producer produces a value, two consumers are waiting
            one consumer if notified, proceeds with de-queuing and notifies on buffer
            the other consumer gets notified and tries to proceed with de-queuing, but the buffer is empty!
           */
          while (buffer.isEmpty) {
            println(s"[consumer $id] buffer empty, waiting...")
            buffer.wait()
          }
          val x = buffer.dequeue()
          println(s"[consumer $id] consumed " + x)

          buffer.notify() // notify somebody to wake up, could be either a consumer or a producer
        }
        Thread.sleep(random.nextInt(500))
      }
    }
  }

  class Producer(id: Int, buffer: mutable.Queue[Int], capacity: Int) extends Thread {
    override def run(): Unit = {
      val random = new Random()
      var i = 0
      while (true) {
        buffer.synchronized {
          while (buffer.size == capacity) {
            println(s"[producer $id] buffer is full, waiting...")
            buffer.wait()
          }

          println(s"[producer $id] producing " + i)
          buffer.enqueue(i)

          buffer.notify()
          i += 1
        }
        Thread.sleep(random.nextInt(250))
      }
    }
  }

  def multiProdCons(nConsumers: Int, nProducers: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 20

    (1 to nConsumers).foreach(i => new Consumer(i, buffer).start())
    (1 to nProducers).foreach(i => new Producer(i, buffer, capacity).start())
  }

  // multiProdCons(6, 3)

  /*
    Exercises:
     1. Think of an example were notifyAll acts in a different way than notify
     2. Create a deadlock
     3. Create a livelock
   */

  // 1. notifyAll()
  def testNotifyAll(): Unit = {
    val bell = new Object

    (1 to 10).foreach(i => new Thread(() => {
      bell.synchronized {
        println(s"[Thread $i] waiting...")
        bell.wait()
        println(s"[Thread $i] hooray!")
      }
    }).start())

    new Thread(() => {
      Thread.sleep(2000)
      println("[announcer] Rock N Roll!")
      bell.synchronized {
        bell.notifyAll() // if this is changed to notify, after 2 seconds only one thread will wake up, and the other 9 threads remain blocked!
      }
    }).start()
  }

  // testNotifyAll()

  /*
   2. deadlock
   A deadlock is a situation in which two computer programs sharing the same resource are effectively preventing each
   other from accessing the resource, resulting in both programs ceasing to function.
   */
  case class Friend(name:String) {
    def bow(other: Friend): Unit = {
      this.synchronized {
        println(s"$this: I am bowing to my friend $other")
        other.rise(this)
        println(s"$this: my friend $other has risen!")
      }
    }

    def rise(other: Friend): Unit = {
      this.synchronized {
        println(s"$this: I am rising to my friend $other")
      }
    }

    var side = "right"
    def switchSide(): Unit = {
      if (side == "right") side = "left"
      else side = "right"
    }

    def pass(other: Friend): Unit = {
      while (this.side == other.side) {
        println(s"$this: Oh, but please, $other, feel free to pass!")
        switchSide()
        Thread.sleep(1000)
      }
    }
  }

  val sam = Friend("Sam")
  val peter = Friend("Peter")

  // two threads lock two objects in reverse order
  def testDeadLock(): Unit = {
    new Thread(() => sam.bow(peter)).start() // sam's lock, then peter's lock
    new Thread(() => peter.bow(sam)).start() // peter's lock, then sam's lock
  }

  // testDeadLock()

  /*
   3. livelock
   A Livelock is a situation where a request for an exclusive lock is denied repeatedly, as many overlapping shared
   locks keep on interfering each other. The processes keep on changing their status, which further prevents them from
   completing the task. This further prevents them from completing the task.
   In a livelock threads yield execution to each other, in such a way that no one can continue.
   The threads are active, but cannot continue.
   */

  def testLiveLock(): Unit = {
    new Thread(() => sam.pass(peter)).start() // sam's lock, then peter's lock
    new Thread(() => peter.pass(sam)).start() // peter's lock, then sam's lock
  }

  testLiveLock()

}
