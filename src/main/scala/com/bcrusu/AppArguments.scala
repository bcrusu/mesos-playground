package com.bcrusu

case class Property(name: String, value: String)

case class AppArguments(properties: Seq[Property]) {
  def getValue(property: String): Option[String] =
    properties.find(_ == property).map(_.name)

  def framework: Option[String] =
    properties.find(_ == "framework").map(_.name)

  def executor: Option[String] =
    properties.find(_ == "executor").map(_.name)

  def help: Boolean =
    properties.find(_ == "help") match {
      case Some(_) => true
      case _ => false
    }
}

object AppArguments {
  def parse(args: Array[String]): Option[AppArguments] = {
    val properties = Seq[Property]()

    try {
      nextOption(properties, args.toSeq)
      Some(AppArguments(properties))
    }
    catch {
      case _: Throwable => None
    }
  }

  private def nextOption(properties: Seq[Property], list: Seq[String]): Seq[Property] =
    list match {
      case Nil => properties
      case "-f" :: value :: tail =>
        nextOption(properties :+ Property("framework", value), tail)
      case "-e" :: value :: tail =>
        nextOption(properties :+ Property("executor", value), tail)
      case "-h" :: tail =>
        nextOption(properties :+ Property("help", ""), tail)
      case option :: tail if option.startsWith("-") =>
        val separatorIndex = option.indexOf("=")

        val name = separatorIndex match {
          case -1 => option.substring(2)
          case _ => option.substring(2, separatorIndex - 1)
        }

        val value = separatorIndex match {
          case -1 => ""
          case _ => option.substring(separatorIndex + 1)
        }

        nextOption(properties :+ Property(name, value), tail)
      case option :: tail =>
        println(s"Unknown option: [$option]")
        properties
    }
}
