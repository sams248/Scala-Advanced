package lectures.part3concurrency

import java.util.concurrent.Executors

object Intro extends App {

  /*
    A thread is an execution context, which is all the information a CPU needs to execute a stream of instructions.
    A thread, in the context of Java, is the path followed when executing a program. All Java programs have at least one
    thread, known as the main thread, which is created by the Java Virtual Machine (JVM) at the program’s start, when
    the main() method is invoked with the main thread.
    In Java, creating a thread is accomplished by implementing an interface and extending a class. Every Java thread is
    created and controlled by the java.lang.Thread class.
    A single-threaded application has only one thread and can handle only one task at a time. To handle multiple tasks
    in parallel, multi-threading is used: multiple threads are created, each performing a different task.
   */

  // JVM threads
  /*
   Thread constructor takes and instance of a trait called runnable
   interface Runnable {
    public void run()
   }
   You can create a new thread simply by extending your class from Thread and overriding it’s run() method.
   The Runnable interface should be implemented by any class whose instances are intended to be executed by a thread.
   The class must define a method of no arguments called run.
   */
  val runnable = new Runnable {
    override def run(): Unit = println("Running in parallel!")
  }
  val aThread = new Thread(runnable)

  // To start a thread, you need to call start method on thread, and not run method on runnable!!
  runnable.run() // doesn't do anything in parallel!
  // Causes this thread to begin execution; the Java Virtual Machine calls the run method of this thread.
  // gives the signal to the JVM to start a JVM thread!
  // creates a JVM thread, which runs on top of operating system thread
  aThread.start()

  // Waits for this thread to die.
  aThread.join() // blocks until aThread finishes running. this is how you make sure a thread has already run before you continue some computation

  // By using Lambda expression, you don’t have to write the boilerplate code: declarations of the anonymous class and the run() method.
  val threadHello = new Thread(() => (1 to 5).foreach(_ => println("hello!"))) // runnable reduced to a lambda
  val threadGoodbye = new Thread(() => (1 to 5).foreach(_ => println("goodbye!")))

  // different runs with multi-threaded environments produce different results
  // thread scheduling depends on a number of factors including the OS and JVM implementations
  threadHello.start()
  threadGoodbye.start()

  // Executes
  // threads are expensive to start and kill, the solution is to reuse them! Java Standard Library offers a nice standard api
  // to reuse threads with executors and thread pools
  val pool = Executors.newFixedThreadPool(10)
  pool.execute(() => println("Something in the thread pool!")) // this (lambda) runnable will be executed by one of the 10 threads managed by this thread pool
  // you don't have to worry about starting an killing threads by using executors

  pool.execute(() => {
    Thread.sleep(1000)
    println("done after 1 second")
  })

  pool.execute(() => {
    Thread.sleep(1000)
    println("almost done")
    Thread.sleep(1000)
    println("done after 2 seconds")
  })

  // pool.shutdown() // no more actions can be submitted
  // pool.execute(() => println("Should not appear!")) // throws java.util.concurrent.RejectedExecutionException in the calling thread
  //println(pool.isShutdown) // true

  // pool.shutdownNow() // interrupts the sleeping threads that are currently running under the pool (if they are sleeping they will throw exceptions)
  // => InterruptedException: sleep interrupted


}
