object Environ {
  def ofXwayland(user: String) = ofPid(activePid(user))

  private[this] def ofPid(pid: String) = {
    val variables = fileToStringList(s"/proc/$pid/environ")
      .flatMap { _.split("\u0000").toList }
      .map     { _.split("=").toList }
      .collect { case key :: value => key -> value.mkString("=") }
      .toMap

    Environ(
      variables("DBUS_SESSION_BUS_ADDRESS"),
      variables("DISPLAY"),
      "/usr/lib/seahorse/ssh-askpass")
  }

  private[this] def activePid(activeUser: String) = OutErr("ps -A -o user= -o pid= -o cmd=").out
    .filter { s => s.startsWith(activeUser) && s.contains("Xwayland") }
    .head.split("\\s+").toList match {
      case user :: pid :: cmd :: display :: _ => pid
      case other => throw new RuntimeException("Cannot parse " + other)
    }

  private[this] def fileToStringList(filename: String) = scala.io.Source
    .fromFile(filename)
    .getLines.toList
    .map(_.trim)
}

case class Environ(dbus: String, display: String, sshAskpass: String)
