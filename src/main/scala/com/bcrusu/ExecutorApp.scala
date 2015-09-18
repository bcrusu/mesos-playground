package com.bcrusu

trait ExecutorApp {
  def run(appArguments : AppArguments): Unit;

  def shutdown(): Unit;
}

object ExecutorApp {
  def apply(executor: String): Option[ExecutorApp] =
    executor match {
      case "test" => Some(new com.bcrusu.test.TestExecutorApp)
      case _ => None
    }
}
