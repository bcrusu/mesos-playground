package com.bcrusu

object App {
  def main(args: Array[String]): Unit =
    AppArguments.parse(args) match {
      case None =>
        displayUsage()
      case Some(appArguments) if appArguments.help =>
        displayUsage()
      case Some(appArguments) =>
        (appArguments.framework, appArguments.executor) match {
          case (Some(_), Some(_)) =>
            println("Cannot start in both framework and executor modes.")
            sys.exit(-1)
          case _ =>
            if (appArguments.framework.isDefined)
              runFramework(appArguments)

            if (appArguments.executor.isDefined)
              runExecutor(appArguments)
        }
    }

  private def displayUsage(): Unit = {
    println("Usage: TODO")
  }

  private def waitEnterKey(): Unit = {
    val NEWLINE = '\n'.toInt
    while (System.in.read != NEWLINE) {
      Thread.sleep(1000)
    }
  }

  private def runFramework(appArguments: AppArguments): Unit =
    FrameworkApp(appArguments.framework.get) match {
      case None =>
        println("Unrecognized framework app.")
        sys.exit(-1)
      case Some(framework) =>
        framework.run(appArguments)
        waitEnterKey()
        framework.shutdown()
    }

  private def runExecutor(appArguments: AppArguments): Unit =
    ExecutorApp(appArguments.executor.get) match {
      case None =>
        println("Unrecognized executor app.")
        sys.exit(-1)
      case Some(executor) =>
        executor.run(appArguments)
        waitEnterKey()
        executor.shutdown()
    }
}
