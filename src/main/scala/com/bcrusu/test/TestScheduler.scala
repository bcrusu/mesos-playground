package com.bcrusu.test

import com.bcrusu.{JavaUtils, MesosUtils}
import com.google.protobuf.ByteString
import org.apache.mesos.Protos.{TaskState, TaskStatus}
import org.apache.mesos._

import scala.collection.JavaConverters._
import scala.collection.mutable

class TestScheduler extends com.bcrusu.Scheduler {
  val TASK_CPUS = 0.5
  val TASK_MEM = 32.0

  private var currentTaskId: Int = 0

  override protected def handleResourceOffer(offer: Protos.Offer): Seq[Protos.TaskInfo] = {
    val maxTasks = MesosUtils.getMaxTasksForOffer(offer, TASK_CPUS, TASK_MEM)
    val result = mutable.Buffer[Protos.TaskInfo]()

    for (_ <- 0 until maxTasks) {
      currentTaskId += 1
      val taskData = TaskRequestData(s"data=${currentTaskId}")
      result += getTaskInfo(offer, currentTaskId, taskData)
    }

    result
  }

  override protected def handleStatusUpdate(taskStatus: TaskStatus): Unit = {
    taskStatus.getState match {
      case TaskState.TASK_FINISHED =>
        val taskResponseData = JavaUtils.toObject[TaskResponseData](taskStatus.getData.toByteArray)
        println(s"Task response: ${taskResponseData.payload}")
      case s: TaskState => println(s"Current task state: ${s}")
    }
  }

  private def getExecutorInfo: Protos.ExecutorInfo = {
    Protos.ExecutorInfo.newBuilder
      .setExecutorId(Protos.ExecutorID.newBuilder.setValue("TestExecutor"))
      .setName("TestExecutor")
      .setCommand(MesosUtils.getExecutorCommand("test"))
      .build
  }

  private def getTaskInfo(offer: Protos.Offer, id: Int, data: TaskRequestData): Protos.TaskInfo = {
    val taskId: String = s"TestTask_$id"

    Protos.TaskInfo.newBuilder
      .setTaskId(Protos.TaskID.newBuilder.setValue(taskId))
      .setName(taskId)
      .setSlaveId(offer.getSlaveId)
      .addAllResources(
        Seq(
          MesosUtils.getScalarResource("cpus", TASK_CPUS),
          MesosUtils.getScalarResource("mem", TASK_MEM)
        ).asJava)
      .setExecutor(getExecutorInfo)
      .setData(ByteString.copyFrom(JavaUtils.toBytes(data)))
      .build
  }
}