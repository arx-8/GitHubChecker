package com.example

import java.io.File

object Const {
  private val PATH_CURRENT_DIR = new File(".").getAbsoluteFile.getParent + "\\"
  val PATH_SETTINGS_FILE = PATH_CURRENT_DIR + "settings.json"
}
