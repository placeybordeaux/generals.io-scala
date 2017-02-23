package horner.ben.generals

import io.socket.emitter.Emitter
import org.json.JSONArray

object Converters {

  def dataToString(data: AnyRef): String = {
    if (data == null) "null"
    else data.toString
  }

  def dataToString(data: Seq[AnyRef]): String = {
    data.map(dataToString).mkString(", ")
  }

  def dataToString(d0: AnyRef, d1: AnyRef): String = dataToString(Seq(d0, d1))

  implicit class FunctionToEmitterListener(f: Seq[AnyRef] => Unit) extends Emitter.Listener {
    override def call(objects: AnyRef*): Unit = f(objects)
  }

  implicit class Function2ToEmitterListener(f: (AnyRef, AnyRef) => Unit) extends Emitter.Listener {
    override def call(objects: AnyRef*): Unit = {
      require(objects.size == 2, "expected 2 argument but got [" + objects.size + "]: " + dataToString(objects))
      f(objects(0), objects(1))
    }
  }

  implicit class Function1ToEmitterListener(f: (AnyRef) => Unit) extends Emitter.Listener {
    override def call(objects: AnyRef*): Unit = {
      require(objects.size == 1, "expected 1 argument but got [" + objects.size + "]: " + dataToString(objects))
      f(objects(0))
    }
  }

  implicit class BlockToEmitterListener(f: () => Unit) extends Emitter.Listener {
    override def call(objects: AnyRef*): Unit = {
      require(objects.size == 0, "expected 0 argument but got [" + objects.size + "]: " + dataToString(objects))
      f()
    }
  }

  implicit def jsonArrayToIntArray(jsonArray: JSONArray): Array[Int] = {
    (0 until jsonArray.length()).map(jsonArray.getInt(_)).toArray
  }

}
