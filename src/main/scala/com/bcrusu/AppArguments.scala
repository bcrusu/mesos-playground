package com.bcrusu

case class Property(name: String, value: String)

case class AppArguments(properties: Seq[Property]) {
  def getValue(propertyName: String): Option[String] =
    getPropertyValue(propertyName)

  def framework: Option[String] =
    getPropertyValue("framework")

  def executor: Option[String] =
    getPropertyValue("executor")

  def classPath: Option[String] =
    getPropertyValue("classpath")

  def mesosMaster: Option[String] =
    getPropertyValue("mesos.master")

  def help: Boolean =
    getPropertyValue("help") match {
      case Some(_) => true
      case _ => false
    }

  private def getPropertyValue(name: String): Option[String] =
    properties.find(_.name == name).map(_.value)
}

object AppArguments {
  def parse(args: Array[String]): Option[AppArguments] = {
    val properties = nextOption(Seq[Property](), args.toList)
    Some(AppArguments(properties))
  }

  private def nextOption(properties: Seq[Property], list: Seq[String]): Seq[Property] =
    list match {
      case Nil => properties
      case option :: tail if option.startsWith("-") =>
        val separatorIndex = option.indexOf("=")

        val name = separatorIndex match {
          case -1 => option.substring(1)
          case _ => option.substring(1, separatorIndex)
        }

        val value = separatorIndex match {
          case -1 => ""
          case _ => option.substring(separatorIndex + 1)
        }

        if (properties.exists(x => x.name == name)) {
          println(s"Ignoring duplicate option: $name='$value'.")
          properties
        }
        else {
          nextOption(properties :+ Property(name, value), tail)
        }
      case option :: tail =>
        println(s"Unknown option: '$option'.")
        properties
    }
}
