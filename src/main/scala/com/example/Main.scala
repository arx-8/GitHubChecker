package com.example

import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods

import scala.io.Source

object Main {
  def main(args: Array[String]): Unit = {
    println(Settings.gitHub.id)
  }
}

case class Settings(gitHubId: String, gitHubPassword: String)

object Settings {
  // TODO catch FileNotFoundException
  private val jsonText = Source.fromFile(Const.PATH_SETTINGS_FILE)
    .getLines()
    // コメントアウト行の除外
    .filter(!_.toString.trim.startsWith("//"))
    .mkString

  // Brings in default date formats etc.
  private implicit val formats = DefaultFormats
  private val values = JsonMethods.parse(jsonText).extract[Values]
  val gitHub: GitHub = values.gitHub

  /**
    * for json extract
    */
  case class Values(gitHub: GitHub)

  case class GitHub(id: String, password: String)

}