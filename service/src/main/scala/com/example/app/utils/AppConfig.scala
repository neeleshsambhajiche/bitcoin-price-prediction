package com.example.app.utils

import java.io.File

import com.typesafe.config.ConfigFactory

/**
 * @since 20/12/20
 */
object AppConfig {

  def config = ConfigFactory.parseFile(new File("app-config.conf"))

  def get(key: String): String = {
    config.getAnyRef(key.toString).toString
  }

}
