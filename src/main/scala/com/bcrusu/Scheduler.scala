package com.bcrusu

import java.util.concurrent.atomic.AtomicInteger

import org.apache.mesos
import org.apache.mesos.{Protos, SchedulerDriver}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

trait Scheduler extends mesos.Scheduler {
  private val tasksCreated = new AtomicInteger(0)
  private val tasksRunning = new AtomicInteger(0)
  private var shuttingDown: Boolean = false

  override final def disconnected(driver: SchedulerDriver): Unit =
    println("Scheduler.disconnected")

  override final def error(driver: SchedulerDriver, msg: String): Unit =
    println(s"Scheduler.error: $msg")

  override final def executorLost(driver: SchedulerDriver, executorId: Protos.ExecutorID, slaveId: Protos.SlaveID, status: Int): Unit =
    println(s"Scheduler.executorLost: executorId=${executorId.getValue}, slaveId=$slaveId, status=$status")

  override final def offerRescinded(driver: SchedulerDriver, offerId: Protos.OfferID): Unit =
    println(s"Scheduler.offerRescinded: offerId=$offerId")

  override final def registered(driver: SchedulerDriver, frameworkId: Protos.FrameworkID, masterInfo: Protos.MasterInfo): Unit = {
    val host = masterInfo.getHostname
    val port = masterInfo.getPort
    println(s"Scheduler.registered: master address=$host:$port")
  }

  override final def reregistered(driver: SchedulerDriver, masterInfo: Protos.MasterInfo): Unit = {
    println("Scheduler.reregistered.")
  }

  override final def slaveLost(driver: SchedulerDriver, slaveId: Protos.SlaveID): Unit =
    println(s"Scheduler.slaveLost: slaveId=$slaveId")

  override final def frameworkMessage(driver: SchedulerDriver, executorId: Protos.ExecutorID, slaveId: Protos.SlaveID, data: Array[Byte]): Unit = {
    println(s"Scheduler.frameworkMessage: executorId=${executorId.getValue}, slaveId=$slaveId")
  }

  override final def resourceOffers(driver: SchedulerDriver, offers: java.util.List[Protos.Offer]): Unit = {
    println(s"Scheduler.resourceOffers: offers count=${offers.size}")

    for (offer <- offers.asScala) {
      if (shuttingDown) {
        println(s"Shutting down: declining offer on [${offer.getHostname}]")
        driver.declineOffer(offer.getId)
      }
      else {
        val tasks = handleResourceOffer(offer)

        if (tasks.nonEmpty) {
          driver.launchTasks(Seq(offer.getId).asJava, tasks.asJava)
          tasksCreated.addAndGet(tasks.length)
        }
        else
          driver.declineOffer(offer.getId)
      }
    }
  }

  override def statusUpdate(driver: SchedulerDriver, taskStatus: Protos.TaskStatus): Unit = {
    val taskId = taskStatus.getTaskId.getValue
    val state = taskStatus.getState
    println(s"Scheduler.statusUpdate: taskId=$taskId, state=$state")

    if (state == Protos.TaskState.TASK_RUNNING)
      tasksRunning.incrementAndGet()
    else if (MesosUtils.isTerminalTaskState(state))
      tasksRunning.decrementAndGet()

    handleStatusUpdate(taskStatus)
  }

  def shutdown[T](maxWait: Duration)(callback: => T): Unit = {
    println("Scheduler.shutdown")
    shuttingDown = true

    val f = Future {
      waitForRunningTasks()
    }

    Try {
      Await.ready(f, maxWait)
    }

    callback
  }

  protected def handleResourceOffer(offer: Protos.Offer): Seq[Protos.TaskInfo]

  protected def handleStatusUpdate(taskStatus: Protos.TaskStatus): Unit

  private def waitForRunningTasks(): Unit = {
    while (tasksRunning.get > 0) {
      println(s"Shutting down but still have $tasksRunning tasks running.")
      Thread.sleep(3000)
    }
  }
}
