enablePlugins(JavaAppPackaging, GraalVMNativeImagePlugin)

name := "yubikey-manager"
organization := "name.bastkowski"
scalaVersion := "2.13.1"
graalVMNativeImageOptions ++= Seq(
  "--initialize-at-build-time",
  "--no-fallback")
