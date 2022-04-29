package lectures.part3concurrency

object JVMConcurrencyProblems {
  // threads on JVM can cause massive bugs

  // variables are the root of almost all evil in parallel and distributed computations
  def runInParallel(): Unit = {
    var x = 0
    val thread1 = new Thread(() => {
      x = 1
    })
    val thread2 = new Thread(() => {
      x = 2
    })

    thread1.start()
    thread2.start()
    Thread.sleep(200)
    println(x)
  }

  case class BankAccount(var amount: Int)

  def buy(bankAccount: BankAccount, thing: String, price: Int): Unit = {
    /*
     involves 3 steps:
      - read old value
      - compute result
      - write new value
     */
    bankAccount.amount -= price
  }

  def buySafe(account: BankAccount, thing: String, price: Int): Unit =
    account.synchronized { // does not allow multiple threads to run critical section at the same time
      account.amount -= price // critical section that can be subject to race condition
      println("I bought " + thing)
      println("My account is now " + account)
    }

  /*
    Example race condition:
      thread1 (shoes)
        - reads amount 50000
        - computes result 50000 - 3000 = 47000
      thread2 (phone)
        - reads amount 50000
        - computes result 50000 - 4000 = 46000
      thread 1 (shoes)
        - write amount 47000
      thread 2 (phone)
       - write amount 46000
   */
  def demoBankingProblem(): Unit = {
    (1 to 10000).foreach { _ =>
      val account = BankAccount(50000)
      val thread1 = new Thread(() => buy(account, "shoes", 3000))
      val thread2 = new Thread(() => buy(account, "phone", 4000))
      thread1.start()
      thread2.start()
      thread1.join()
      thread2.join()
      if (account.amount != 50000 - 3000 - 4000) println(s"I just broke the bank: ${account.amount}")
    }
  }

  // Exercises
  /*
      1. Create "inception threads
        thread 1
          -> thread 2
            -> thread 3
              ...
      each thread prints "hello from thread $i"
      Print all messages in reverse order
   */
  def inceptionThreads(maxThreads: Int, i: Int = 1): Thread = {
    new Thread(() => {
      if (i < maxThreads) {
        val newThread = inceptionThreads(maxThreads, i + 1)
        newThread.start()
        newThread.join()
      }
      println(s"Hello from thread $i")
    })
  }

  /*
    2. what's the max/min value of x

    max value = 100 - each thread increases x by 1
    min value = 1:
      all threads read x = 0 at the same time
      all threads (in parallel) compute 0 + 1 = 1
      all threads try to write x = 1
   */

  def minMax(): Unit = {
    var x = 0
    val threads = (1 to 100).map(_ => new Thread(() => x += 1))
    threads.foreach(_.start())
  }

  /*
    3. Sleep fallacy: what's the value of message?
    almost always, message = "Scala is awesome!"
    is it guaranteed? No!
    Obnoxious situation:

      main thread:
        message = "Scala sucks"
        awesomeThread.start()
        sleep(1001) - [On some JVMs, some OSs] yields execution: puts the thread on old, schedules another thread for execution
      awesome thread starts:
        sleep(1000) - yields execution (processor takes another thread from OS)
      OS gives the CPU to some important thread - takes more than 2 seconds
      OS gives the CPU back to the MAIN thread
      main thread prints "Scala sucks"
      only after that OS gives the CPU to awesomeThread:
      message = "Scala is awesome" (this assignment is done too late, message is already printed)
   */

  // how do we fix this? synchronizing does NOT work => line 134
  def demoSleepFallacy(): Unit = {
    var message = ""
    val awesomeThread = new Thread(() => {
      Thread.sleep(1000)
      message = "Scala is awesome!"
    })
    message = "Scala sucks!"
    awesomeThread.start()
    Thread.sleep(1001)
    // solution: join the worker thread: awesomeThread.join()
    println(message)
  }

  def main(args: Array[String]): Unit = {
    // runInParallel() // race condition
    // demoBankingProblem()
    // inceptionThreads(50).start()
    demoSleepFallacy()
  }

}
