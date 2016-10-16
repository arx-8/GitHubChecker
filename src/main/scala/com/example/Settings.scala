package com.example

import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods

import scala.io.Source

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
  val pullreq: Pullreq = values.pullreq
  val slack: Slack = values.slack

  /**
    * for json extract
    */
  case class Values(gitHub: GitHub, pullreq: Pullreq, slack: Slack)

  case class GitHub(id: String, password: String)

  case class Pullreq(
      taskKeyRegexFormat: String,
      ignoreTaskKeyList: List[String],
      labelWaitingMerge: String,
      labelWaitingFix: String,
      labelUnusedList: List[String],
      labelReviewerList: List[String]
  )

  case class Slack(apiToken: String, postChannel: String)

}
