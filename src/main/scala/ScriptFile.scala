import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Path, Files}
import java.nio.file.attribute.PosixFilePermission._

object ScriptFile {
  def apply(prefix: String, cmd: String) = {
    val file = File.createTempFile(prefix, ".sh")
    file.deleteOnExit()

    Files.write(file.toPath, cmd.getBytes(UTF_8))
    val permissions = Files.getPosixFilePermissions(file.toPath)
    permissions.add(OWNER_EXECUTE)
    permissions.add(GROUP_EXECUTE)
    permissions.add(OTHERS_EXECUTE)
    Files.setPosixFilePermissions(file.toPath, permissions)
    file
  }
}
