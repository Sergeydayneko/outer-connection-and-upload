package com.altuera

import com.typesafe.config.{Config, ConfigFactory}

object Utility {
  val serverConf: Config = ConfigFactory.load().getConfig("server")
  val fileConf: Config   = ConfigFactory.load().getConfig("file")

  lazy val rootFolder: String = serverConf.getString("root_folder")
  lazy val fileSuffix: String = fileConf.getString("suffix")
  lazy val filePrefix: String = fileConf.getString("prefix")

}
