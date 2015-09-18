package com.bcrusu.test

import com.bcrusu.AppArguments
import org.apache.mesos.{Protos, MesosSchedulerDriver, SchedulerDriver}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class TestFrameworkApp extends com.bcrusu.FrameworkApp {
  private lazy val frameworkInfo: Protos.FrameworkInfo =
    Protos.FrameworkInfo.newBuilder
      .setName("TEST1")
      .setFailoverTimeout(60.seconds.toMillis)
      .setCheckpoint(false)
      .setUser("")
      .build

  var running: Boolean = false
  var scheduler: TestScheduler = null
  var driver: SchedulerDriver = null

  override def run(appArguments: AppArguments): Unit =
    running match {
      case true => throw new IllegalStateException("Framework running.")
      case false =>
        val mesosMaster = appArguments.getValue("mesos.master").getOrElse(throw new IllegalArgumentException("mesos.master property not set"))

        scheduler = new TestScheduler
        driver = new MesosSchedulerDriver(scheduler, frameworkInfo, mesosMaster)

        Future {
          driver.run
        }
    }

  override def shutdown(): Unit =
    running match {
      case false => throw new IllegalStateException("Framework not running.")
      case true =>
        scheduler.shutdown(5.minutes) {
          driver.stop()
        }
    }
}
