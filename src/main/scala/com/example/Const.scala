package com.example

import java.io.File

object Const {
  // local path
  private val PATH_CURRENT_DIR = new File(".").getAbsoluteFile.getParent + "\\"
  val PATH_SETTINGS_FILE = PATH_CURRENT_DIR + "settings.json"

  // url
  val URL_GITHUB_LOGIN = "https://github.com/login"
  val URL_GITHUB_PULLREQ = "https://github.com/pulls"
}
