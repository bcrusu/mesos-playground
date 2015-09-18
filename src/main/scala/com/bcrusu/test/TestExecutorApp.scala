package com.bcrusu.test

import com.bcrusu.AppArguments
import org.apache.mesos.MesosExecutorDriver

class TestExecutorApp extends com.bcrusu.ExecutorApp {
  var running: Boolean = false
  var executor: TestExecutor = null
  var driver: MesosExecutorDriver = null

  override def run(appArguments: AppArguments): Unit =
    running match {
      case true => throw new IllegalStateException("Executor running.")
      case false =>
        executor = new TestExecutor
        driver = new MesosExecutorDriver(executor)
        driver.run()
    }

  override def shutdown(): Unit =
    running match {
      case false => throw new IllegalStateException("Executor not running.")
      case true =>
        executor.shutdown(driver)
        driver.stop()
    }
}
