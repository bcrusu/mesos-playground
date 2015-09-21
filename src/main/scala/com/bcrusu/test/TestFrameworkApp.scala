package com.bcrusu.test

import com.bcrusu.Scheduler
import org.apache.mesos.Protos
import org.apache.mesos.Protos.FrameworkInfo

import scala.concurrent.duration._

class TestFrameworkApp extends com.bcrusu.FrameworkApp {
  override def createScheduler(): Scheduler = new TestScheduler

  override def createFrameworkInfo(): FrameworkInfo =
    Protos.FrameworkInfo.newBuilder
      .setName("TestFramework")
      .setFailoverTimeout(60.seconds.toMillis)
      .setCheckpoint(false)
      .setUser("")
      .build
}
