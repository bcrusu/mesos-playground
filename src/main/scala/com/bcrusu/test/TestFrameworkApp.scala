package com.bcrusu.test

import com.bcrusu.Scheduler
import org.apache.mesos.Protos
import org.apache.mesos.Protos.FrameworkInfo

class TestFrameworkApp extends com.bcrusu.FrameworkApp {
  override def createScheduler(): Scheduler = new TestScheduler

  override def createFrameworkInfo(): FrameworkInfo =
    Protos.FrameworkInfo.newBuilder
      .setName("TestFramework")
      .setFailoverTimeout(0)    //seconds
      .setCheckpoint(false)
      .setUser("root")
      .build
}
