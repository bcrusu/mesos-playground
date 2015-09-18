package com.bcrusu.test

import org.apache.mesos._

import scala.collection.JavaConverters._
import scala.collection.mutable

class TestScheduler() extends com.bcrusu.Scheduler {
  val TASK_CPUS = 0.1
  val TASK_MEM = 32.0

  private val taskQueue = mutable.Queue[String]()

  override protected def handleResourceOffer(offer: Protos.Offer): Seq[Protos.TaskInfo] = {
    val maxTasks = getMaxTasksForOffer(offer)

    val result = mutable.Buffer[Protos.TaskInfo]()

    for (_ <- 0 until maxTasks) {
      if (taskQueue.nonEmpty) {
        val task = taskQueue.dequeue
        result += getTaskInfo("TODO", offer)
      }
    }

    result
  }

  private lazy val executor: Protos.ExecutorInfo = {
    val command = Protos.CommandInfo.newBuilder
      .setValue("python render_executor.py --local")

    Protos.ExecutorInfo.newBuilder
      .setExecutorId(Protos.ExecutorID.newBuilder.setValue("render-executor"))
      .setName("Renderer")
      .setCommand(command)
      .build
  }

  private def getMaxTasksForOffer(offer: Protos.Offer): Int = {
    var count = 0
    var cpus = 0.0
    var mem = 0.0

    for (resource <- offer.getResourcesList.asScala) {
      resource.getName match {
        case "cpus" => cpus = resource.getScalar.getValue
        case "mem" => mem = resource.getScalar.getValue
        case _ => ()
      }
    }

    while (cpus >= TASK_CPUS && mem >= TASK_MEM) {
      count = count + 1
      cpus = cpus - TASK_CPUS
      mem = mem - TASK_MEM
    }

    count
  }

  private def getTaskInfo(id: String, offer: Protos.Offer): Protos.TaskInfo =
    getTaskInfoPrototype(id, offer).toBuilder
      .setName(s"render_$id")
      .setExecutor(executor)
      //.setData(ByteString.copyFromUtf8(url))
      .build

  private def getTaskInfoPrototype(id: String, offer: Protos.Offer): Protos.TaskInfo =
    Protos.TaskInfo.newBuilder
      .setTaskId(Protos.TaskID.newBuilder.setValue(id))
      .setName("")
      .setSlaveId((offer.getSlaveId))
      .addAllResources(
        Seq(
          getScalarResource("cpus", TASK_CPUS),
          getScalarResource("mem", TASK_MEM)
        ).asJava
      )
      .build

  private def getScalarResource(name: String, value: Double): Protos.Resource =
    Protos.Resource.newBuilder
      .setType(Protos.Value.Type.SCALAR)
      .setName(name)
      .setScalar(Protos.Value.Scalar.newBuilder.setValue(value))
      .build
}