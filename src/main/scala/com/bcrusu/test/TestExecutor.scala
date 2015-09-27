package com.bcrusu.test

import com.bcrusu.JavaUtils
import com.google.protobuf.ByteString
import org.apache.mesos.Protos.{TaskID, TaskInfo, TaskState}
import org.apache.mesos.{ExecutorDriver, Protos}

import scala.util.Random

class TestExecutor extends com.bcrusu.Executor {
  override def launchTask(executorDriver: ExecutorDriver, taskInfo: TaskInfo): Unit = {
    val taskRequestData = JavaUtils.toObject[TaskRequestData](taskInfo.getData.toByteArray)
    val executorId = taskInfo.getExecutor.getExecutorId.getValue
    val taskResponseData = TaskResponseData(s"${taskRequestData.payload} from executor '$executorId'")

    val taskStatus = getTaskStatus(taskInfo, taskResponseData)

    //simulate work...
    val random = Random
    Thread.sleep(3000 + (random.nextDouble() * 3000).toInt)

    executorDriver.sendStatusUpdate(taskStatus)
  }

  private def getTaskStatus(taskInfo: TaskInfo, response: TaskResponseData): Protos.TaskStatus = {
    Protos.TaskStatus.newBuilder
      .setTaskId(TaskID.newBuilder.setValue(taskInfo.getTaskId.getValue))
      .setState(TaskState.TASK_FINISHED)
      .setData(ByteString.copyFrom(JavaUtils.toBytes(response)))
      .build
  }
}
