package com.bcrusu

import org.apache.mesos
import org.apache.mesos.ExecutorDriver
import org.apache.mesos.Protos._

trait Executor extends mesos.Executor {
  override def shutdown(executorDriver: ExecutorDriver): Unit = {
    println("Executor.shutdown")
  }

  override def disconnected(executorDriver: ExecutorDriver): Unit = {
    println("Executor.disconnected")
  }

  override def killTask(executorDriver: ExecutorDriver, taskID: TaskID): Unit = {
    println("Executor.killTask")
  }

  override def reregistered(executorDriver: ExecutorDriver, slaveInfo: SlaveInfo): Unit = {
    println("Executor.reregistered")
  }

  override def error(executorDriver: ExecutorDriver, s: String): Unit = {
    println("Executor.error")
  }

  override def frameworkMessage(executorDriver: ExecutorDriver, bytes: Array[Byte]): Unit = {
    println("Executor.frameworkMessage")
  }

  override def registered(executorDriver: ExecutorDriver, executorInfo: ExecutorInfo, frameworkInfo: FrameworkInfo, slaveInfo: SlaveInfo): Unit = {
    println("Executor.registered")
  }

  override def launchTask(executorDriver: ExecutorDriver, taskInfo: TaskInfo): Unit = {
    println("Executor.launchTask")
  }
}
