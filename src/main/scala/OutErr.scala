import scala.collection.mutable.ArrayBuffer
import sys.process._

object OutErr {
  def apply(cmd: String): OutErr = {
    val out = ArrayBuffer.empty[String]
    val err = ArrayBuffer.empty[String]
    val exitCode = cmd ! ProcessLogger(out.append, err.append)
    if (exitCode != 0) throw new RuntimeException(s"Could not execute ${cmd}\nstderr:\n${err.mkString("\n")}")
    OutErr(out.toList, err.toList)
  }
}

case class OutErr(out: List[String], err: List[String])
