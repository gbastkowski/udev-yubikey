object Main {

  lazy val activeUser = {
    val activeTty = fileToStringList("/sys/class/tty/tty0/active")

    OutErr("who").out.map(_.split("\\s+").toList)
      .collect { case user :: tty :: _ if tty == activeTty.head => user }
      .head
  }

  def main(args: Array[String]): Unit = {
    args(0) match {
      case "add"    => add()
      case "remove" => remove()
      case "test"   => test()
      case other    => throw new IllegalArgumentException("Please run with add, remove, or test")
    }
  }

  def add() = {
    val message = """
      |Device attached.
      |Adding private key from YubiKey.
      |You'll be asked to enter the PIN.""".stripMargin

    OutErr(su(activeUser, ScriptFile("yubikey-notification", sendNotification(message)).toString))
    OutErr(su(activeUser, ScriptFile("yubikey-ssh-add",      sshAdd()).toString))
  }

  def remove() = {
    val message = """
		  |Device removed.
      |Removing cached YubiKey SSH keys.""".stripMargin
    OutErr(su(activeUser, ScriptFile("yubikey-notification", sendNotification(message)).toString))
    OutErr(su(activeUser, ScriptFile("yubikey-ssh-remove",   sshRemove()).toString))
  }

  def test() = {
    val scriptFile =
      ScriptFile("yubikey-notification",
        sendNotification(
          "This is a test\n" +
          "Vivamus id enim.\n" +
          "Fusce suscipit, wisi nec facilisis facilisis, est dui fermentum leo, quis tempor ligula erat quis odio."))

    val suCmd = su(activeUser, scriptFile.toString())

    OutErr(suCmd) match {
      case OutErr(out, err) =>
        println("--- out ---")
        println(out)
        println("--- err ---")
        println(err)
    }
  }

  def su(user: String, cmd: String) = s"""/bin/su $user -c "$cmd""""

  def sshAdd() =  scriptWithRelevantEnvironment(s"""ssh-add -s /usr/lib/libykcs11.so""")

  def sshRemove() =  scriptWithRelevantEnvironment(s"""ssh-add -e /usr/lib/libykcs11.so""")

  def sendNotification(message: String) = scriptWithRelevantEnvironment(s"""notify-send -t 2000 "YubiKey" "$message"""")

  def scriptWithRelevantEnvironment(cmd: String) = "#!/bin/sh\n" + relevantEnvironment + " " + cmd + "\n"

  def relevantEnvironment = {
    val e = Environ.ofXwayland(activeUser)
    s"""DBUS_SESSION_BUS_ADDRESS="${e.dbus}" DISPLAY="${e.display}" SSH_ASKPASS="${e.sshAskpass}""""
  }

  def fileToStringList(filename: String) = scala.io.Source
    .fromFile(filename)
    .getLines.toList
    .map(_.trim)
}
