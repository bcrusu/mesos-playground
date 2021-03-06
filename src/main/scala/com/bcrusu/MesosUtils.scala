package com.bcrusu

import org.apache.mesos.Protos
import scala.collection.JavaConverters._

private object MesosUtils {
  def getScalarResource(name: String, value: Double): Protos.Resource =
    Protos.Resource.newBuilder
      .setType(Protos.Value.Type.SCALAR)
      .setName(name)
      .setScalar(Protos.Value.Scalar.newBuilder.setValue(value))
      .build

  def getMaxTasksForOffer(offer: Protos.Offer, taskCpu: Double, taskMem: Double): Int = {
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

    while (cpus >= taskCpu && mem >= taskMem) {
      count = count + 1
      cpus = cpus - taskCpu
      mem = mem - taskMem
    }

    count
  }

  def isTerminalTaskState(state: Protos.TaskState): Boolean = {
    import Protos.TaskState._
    state match {
      case TASK_FINISHED | TASK_FAILED | TASK_KILLED | TASK_LOST =>
        true
      case _ =>
        false
    }
  }

  def getExecutorCommand(executor: String): Protos.CommandInfo = {
    val classpath = s"/cluster/mesosTest.jar:/usr/share/scala/lib/*"
    Protos.CommandInfo.newBuilder
      .setValue(s"java -cp ${classpath} ${AppConstants.AppEntryPoint} -executor=${executor}")
      .build
  }
}
