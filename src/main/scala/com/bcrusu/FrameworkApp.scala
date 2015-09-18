package com.bcrusu

trait FrameworkApp {
  def run(appArguments : AppArguments) : Unit;

  def shutdown() : Unit;
}

object FrameworkApp {
  def apply(framework: String): Option[FrameworkApp] =
    framework match {
      case "test" => Some(new com.bcrusu.test.TestFrameworkApp)
      case _ => None
    }
}
