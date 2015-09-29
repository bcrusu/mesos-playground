package com.bcrusu

import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}

private object JavaUtils {
  def using[A <: java.lang.AutoCloseable, B](closeable: => A)(func: A => B): B = {
    val c = closeable
    try {
      func(c)
    }
    finally {
      c.close()
    }
  }

  def toBytes[A <: Serializable](obj: A): Array[Byte] =
    using(new ByteArrayOutputStream())(os => {
      using(new ObjectOutputStream(os))(oos => {
        oos.writeObject(obj)
      })

      os.toByteArray
    })

  def toObject[A <: Serializable](bytes: Array[Byte]): A =
    using(new ByteArrayInputStream(bytes))(is => {
      using(new ObjectInputStream(is))(ois => {
        ois.readObject().asInstanceOf[A]
      })
    })
}
