package com.example

import com.example.controller.GitHubController

object Main {
  def main(args: Array[String]): Unit = {
    // selenium debug log の抑制
    java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.SEVERE)

    new GitHubController
  }
}
