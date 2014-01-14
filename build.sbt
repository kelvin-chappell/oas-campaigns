name := "oas-campaigns"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.apache.axis" % "axis" % "1.4",
  "org.apache.axis" % "axis-jaxrpc" % "1.4",
  "wsdl4j" % "wsdl4j" % "1.6.3",
  "log4j" % "log4j" % "1.2.17",
  "commons-logging" % "commons-logging" % "1.1.3",
  "commons-discovery" % "commons-discovery" % "0.5",
  "javax.mail" % "mail" % "1.4.7"
)
