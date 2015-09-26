package com.bcrusu

import org.apache.mesos.MesosExecutorDriver

trait ExecutorApp {
  var running: Boolean = false
  var executor: Executor = null
  var driver: MesosExecutorDriver = null

  final def run(appArguments: AppArguments): Unit =
    running match {
      case true => throw new IllegalStateException("Executor running.")
      case false =>
        executor = createExecutor()
        driver = new MesosExecutorDriver(executor)

        val runStatus = driver.run()
        Console.println(s"Driver disconnected with status '$runStatus'.")
    }

  def createExecutor(): Executor
}

object ExecutorApp {
  def apply(executor: String): Option[ExecutorApp] =
    executor match {
      case "test" => Some(new com.bcrusu.test.TestExecutorApp)
      case _ => None
    }
}
