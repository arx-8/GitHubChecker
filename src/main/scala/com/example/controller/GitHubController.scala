package com.example.controller

import com.example.controller.StatusEnum.Status
import com.example.{Const, Enum, Settings}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.{By, WebElement}

import scala.collection.JavaConversions._
import scala.collection.mutable

class GitHubController {
  private val driver = new HtmlUnitDriver {
    get(Const.URL_GITHUB_LOGIN)
    // TODO github側のDOM構造変更時の検知
    findElementByCssSelector("#login_field").sendKeys(Settings.gitHub.id)
    findElementByCssSelector("#password").sendKeys(Settings.gitHub.password)
    findElementByCssSelector("[name=commit]").click()
    get(Const.URL_GITHUB_PULLREQ)
    if (getTitle.startsWith("Page not found")) {
      throw new IllegalArgumentException(s"ログインに失敗。id=${Settings.gitHub.id}")
    }
    println("ログイン成功")
  }

  def getPullreqList: List[PullreqDto] = {
    // get pullreq's
    driver.get(Const.URL_GITHUB_PULLREQ)
    val prEleList = findElementsByCssSelector4s(driver, ".js-navigation-container .js-issue-row")

    // convert
    val prList = prEleList
      .map(pr => {
        // 1つの要素からいくつか取り出せるので多値返却
        val issueTitleEle = findElementByCssSelector4s(pr, ".js-navigation-open")
        val (url, taskKey, taskName) = issueTitleEle match {
          case Some(e) => {
            // href link
            val url = e.getAttribute("href")

            // taskKey, taskName
            val rgx = Settings.pullreq.taskKeyRegexFormat.r
            val taskKey = rgx.findFirstIn(e.getText.trim)
            val taskName = rgx.replaceFirstIn(e.getText.trim, "").trim
            taskKey match {
              case Some(_taskKey) => (url, _taskKey, taskName)
              case None => throw new DomChangedException("taskKey is Not Found.")
            }
          }
          case None => throw new DomChangedException("issueTitleEle is Not Found.")
        }

        // labels
        val labels = findElementsByCssSelector4s(pr, ".labels a").map(_.getText.trim)

        // TODO 詳細ページにアクセスして、情報を読み取る


        new PullreqDto(url, taskKey, taskName, labels)
      })
      .filter(p => {
        !Settings.pullreq.ignoreTaskKeyList.contains(p.taskKey)
      })

    return prList
  }

  private class DomChangedException(message: String) extends Exception {
  }

  /**
    * HtmlUnitDriverの同名メソッドをScalaで扱い易くするためのラッパー
    * TODO be implicit
    *
    * @param using
    * @param driver
    * @return
    */
  private def findElementByCssSelector4s(driver: HtmlUnitDriver, using: String): Option[WebElement] = {
    Option(driver.findElementByCssSelector(using))
  }

  /**
    * HtmlUnitDriverの同名メソッドをScalaで扱い易くするためのラッパー
    * TODO be implicit
    *
    * @param using
    * @param driver
    * @return
    */
  private def findElementsByCssSelector4s(driver: HtmlUnitDriver, using: String): List[WebElement] = {
    val elements = Option(driver.findElementsByCssSelector(using))
    elements match {
      case Some(e) => e.toList
      case None => List.empty
    }
  }

  /**
    * WebElementの同名メソッドをScalaで扱い易くするためのラッパー
    * TODO be implicit
    *
    * @param element
    * @param using
    * @return
    */
  private def findElementByCssSelector4s(element: WebElement, using: String): Option[WebElement] = {
    Option(element.findElement(By.cssSelector(using)))
  }

  /**
    * WebElementの同名メソッドをScalaで扱い易くするためのラッパー
    * TODO be implicit
    *
    * @param element
    * @param using
    * @return
    */
  private def findElementsByCssSelector4s(element: WebElement, using: String): List[WebElement] = {
    val elements = Option(element.findElements(By.cssSelector(using)))
    elements match {
      case Some(e) => e.toList
      case None => List.empty
    }
  }
}

class PullreqDto(
    val url: String,
    val taskKey: String,
    val taskName: String,
    val labelList: List[String]) {

  val statusList = getStatusList

  /**
    * TODO より詳細に分類する必要がある
    *
    * @return
    */
  private def getStatusList: List[Status] = {
    var statusList = mutable.ListBuffer.empty[Status]

    // 未使用ラベルチェック
    if (0 < Settings.pullreq.labelUnusedList.intersect(labelList).length) {
      statusList += StatusEnum.InvalidDetectUnusedLabel
    }

    // レビュー指摘対応が必要
    if (labelList.contains(Settings.pullreq.labelWaitingFix)) {
      statusList += StatusEnum.InvalidWaitingFix
    }

    // マージ待ちラベルが必要か
    if (!labelList.contains(Settings.pullreq.labelWaitingMerge)) {
      if (2 <= Settings.pullreq.labelReviewerList.intersect(labelList).length) {
        statusList += StatusEnum.InvalidNeedLabelWaitingMarge
      } else {
        statusList += StatusEnum.ValidWaitingReview
      }
    }

    // どの条件にも合致しない場合、リリース待ち
    if (statusList.isEmpty) {
      statusList += StatusEnum.ValidWaitingRelease
    }

    statusList.toList
  }
}

object StatusEnum extends Enum {
  override def values: List[Status] = List()

  sealed abstract class Status(val value: String)

  case object ValidWaitingReview extends Status("レビュー待ち")

  case object ValidWaitingRelease extends Status("リリース待ち")

  case object ValidRemainingTask extends Status("残タスク")

  case object InvalidWaitingFix extends Status("レビュー指摘対応が必要")

  case object InvalidNeedLabelWaitingMarge extends Status("【警告】「マージ待ち」ラベルが必要")

  case object InvalidDetectUnusedLabel extends Status("【警告】変なラベルが付いてる")

}
