package com.bcrusu

import java.nio.charset.Charset

import org.apache.mesos
import org.apache.mesos.{Protos, SchedulerDriver}

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

trait Scheduler extends mesos.Scheduler {
  private var tasksCreated = 0
  private var tasksRunning = 0
  private var shuttingDown: Boolean = false

  override final def disconnected(driver: SchedulerDriver): Unit =
    println("Disconnected from the Mesos master...")

  override final def error(driver: SchedulerDriver, msg: String): Unit =
    println(s"ERROR: [$msg]")

  override final def executorLost(driver: SchedulerDriver, executorId: Protos.ExecutorID, slaveId: Protos.SlaveID, status: Int): Unit =
    println(s"EXECUTOR LOST: [${executorId.getValue}]")

  override final def offerRescinded(driver: SchedulerDriver, offerId: Protos.OfferID): Unit =
    println(s"Offer [${offerId.getValue}] has been rescinded")

  override final def registered(driver: SchedulerDriver, frameworkId: Protos.FrameworkID, masterInfo: Protos.MasterInfo): Unit = {
    val host = masterInfo.getHostname
    val port = masterInfo.getPort
    println(s"Registered with Mesos master [$host:$port]")
  }

  override final def reregistered(driver: SchedulerDriver, masterInfo: Protos.MasterInfo): Unit = {
    println("reregistered.")
  }

  override final def slaveLost(driver: SchedulerDriver, slaveId: Protos.SlaveID): Unit =
    println(s"SLAVE LOST: [${slaveId.getValue}]")

  override final def frameworkMessage(driver: SchedulerDriver, executorId: Protos.ExecutorID, slaveId: Protos.SlaveID, data: Array[Byte]): Unit = {
    println(s"Received a framework message from [${executorId.getValue}]")

    //TODO
    val jsonString = new String(data, Charset.forName("UTF-8"))

    executorId.getValue match {
      case id if id == "RefreshExecutorId" =>

      case _ => ()
    }
  }

  override final def resourceOffers(driver: SchedulerDriver, offers: java.util.List[Protos.Offer]): Unit = {
    for (offer <- offers.asScala) {
      println(s"Got resource offer [$offer]")

      if (shuttingDown) {
        println(s"Shutting down: declining offer on [${offer.getHostname}]")
        driver.declineOffer(offer.getId)
      }
      else {
        val tasks = handleResourceOffer(offer)

        if (tasks.nonEmpty) {
          driver.launchTasks(Seq(offer.getId).asJava, tasks.asJava)
          tasksCreated += tasks.length
        }
        else
          driver.declineOffer(offer.getId)
      }
    }
  }

  override def statusUpdate(driver: SchedulerDriver, taskStatus: Protos.TaskStatus): Unit = {
    val taskId = taskStatus.getTaskId.getValue
    val state = taskStatus.getState
    println(s"Task [$taskId] is in state [$state]")

    if (state == Protos.TaskState.TASK_RUNNING)
      tasksRunning = tasksRunning + 1
    else if (MesosUtils.isTerminalTaskState(state))
      tasksRunning = math.max(0, tasksRunning - 1)
  }

  def shutdown[T](maxWait: Duration)(callback: => T): Unit = {
    println("Scheduler shutting down...")
    shuttingDown = true

    val f = Future {
      waitForRunningTasks()
    }

    Try {
      Await.ready(f, maxWait)
    }

    callback
  }

  protected def handleResourceOffer(offer: Protos.Offer): Seq[Protos.TaskInfo];

  private def waitForRunningTasks(): Unit = {
    while (tasksRunning > 0) {
      println(s"Shutting down but still have $tasksRunning tasks running.")
      Thread.sleep(3000)
    }
  }
}
