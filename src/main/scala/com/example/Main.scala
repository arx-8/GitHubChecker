package com.example

import com.example.controller.{GitHubController, PullreqDto, SlackController, StatusEnum}

object Main {
  def main(args: Array[String]): Unit = {
    // selenium debug log の抑制
    java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.SEVERE)

    checkLoop(new GitHubController, new SlackController)
  }

  val WAIT_TIME_MSEC: Long = 5 * 60 * 1000

  def checkLoop(gitHub: GitHubController, slack: SlackController): Unit = {
    while (true) {
      // 通知したい状態のプルリクを選ぶ
      val pullreqList = gitHub.fetchPullreqList.filter(_.needNoticeList.nonEmpty)

      slack.sendMessage(makeMessage(pullreqList))

      Thread.sleep(WAIT_TIME_MSEC)

      // TODO 安定して実行できるようになったら常駐無限ループ
      return
    }
  }

  def makeMessage(pullreqList: List[PullreqDto]): String = {
    pullreqList.map(p => {
      var msg = ""
      msg += p.taskKey + " "

      p.needNoticeList.foreach {
        case StatusEnum.InvalidConflict => msg += "コンフリクトしてる" + " "

        case _ => // TODO other
      }

      msg += p.url + " "

      return msg
    }).mkString
  }
}
