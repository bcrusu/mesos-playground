package com.bcrusu.test

import com.bcrusu.MesosUtils
import org.apache.mesos._

import scala.collection.JavaConverters._
import scala.collection.mutable

class TestScheduler() extends com.bcrusu.Scheduler {
  val TASK_CPUS = 0.1
  val TASK_MEM = 32.0

  private val taskQueue = mutable.Queue[String]()
  private var currentTaskId: Int = 0

  override protected def handleResourceOffer(offer: Protos.Offer): Seq[Protos.TaskInfo] = {
    val maxTasks = 0
      //TODO: MesosUtils.getMaxTasksForOffer(offer, TASK_CPUS, TASK_MEM)

    val result = mutable.Buffer[Protos.TaskInfo]()

    for (_ <- 0 until maxTasks) {
      if (taskQueue.nonEmpty) {
        val task = taskQueue.dequeue
        currentTaskId += 1
        result += getTaskInfo(currentTaskId, offer)
      }
    }

    result
  }

  private lazy val executorInfo: Protos.ExecutorInfo = {
    val command = Protos.CommandInfo.newBuilder
      .setValue("java -cp /cluster/mesosTest.jar:/usr/share/scala/lib/* com.bcrusu.App") //TODO: path the executor app

    //TODO /usr/share/java/mesos-0.23.0.jar

    Protos.ExecutorInfo.newBuilder
      .setExecutorId(Protos.ExecutorID.newBuilder.setValue("TestExecutor"))
      .setName("TestExecutor")
      .setCommand(command)
      .build
  }

  private def getTaskInfo(id: Int, offer: Protos.Offer): Protos.TaskInfo =
    Protos.TaskInfo.newBuilder
      .setTaskId(Protos.TaskID.newBuilder.setValue(s"TestTask_$id"))
      .setName(s"TestTask_$id")
      .setSlaveId(offer.getSlaveId)
      .addAllResources(
        Seq(
          MesosUtils.getScalarResource("cpus", TASK_CPUS),
          MesosUtils.getScalarResource("mem", TASK_MEM)
        ).asJava
      )
      .setExecutor(executorInfo)
      //.setData(ByteString.copyFromUtf8(url))
      .build
}