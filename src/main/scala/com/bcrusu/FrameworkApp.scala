package com.bcrusu

import org.apache.mesos.{Protos, MesosSchedulerDriver, SchedulerDriver}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait FrameworkApp {
  var running: Boolean = false
  var scheduler: Scheduler = null
  var driver: SchedulerDriver = null

  final def run(appArguments: AppArguments): Unit =
    running match {
      case true => throw new IllegalStateException("Framework running.")
      case false =>
        val mesosMaster = appArguments.getValue("mesos.master").getOrElse(throw new IllegalArgumentException("mesos.master property not set"))

        val frameworkInfo = createFrameworkInfo()
        scheduler = createScheduler()
        driver = new MesosSchedulerDriver(scheduler, frameworkInfo, mesosMaster)

        Future {
          driver.run
        }
    }

  final def shutdown(): Unit =
    running match {
      case false => throw new IllegalStateException("Framework not running.")
      case true =>
        scheduler.shutdown(5.minutes) {
          driver.stop()
        }
    }

  def createScheduler(): Scheduler

  def createFrameworkInfo() : Protos.FrameworkInfo
}

object FrameworkApp {
  def apply(framework: String): Option[FrameworkApp] =
    framework match {
      case "test" => Some(new com.bcrusu.test.TestFrameworkApp)
      case _ => None
    }
}
