package com.example.controller

import com.example.{Const, Settings}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.{By, WebElement}

import scala.collection.JavaConversions._

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
  }

  // get pullreq's
  private val prEleList = findElementsByCssSelector4s(driver, ".js-navigation-container .js-issue-row")

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
          val r = Settings.pullreq.taskKeyRegexFormat.r
          val taskKey = r.findFirstIn(e.getText.trim)
          val taskName = r.replaceFirstIn(e.getText.trim, "").trim
          taskKey match {
            case Some(_taskKey) => (url, _taskKey, taskName)
            case None => throw new DomChangedException("taskKey is Not Found.")
          }
        }
        case None => throw new DomChangedException("issueTitleEle is Not Found.")
      }

      // labels
      val labels = findElementsByCssSelector4s(pr, ".labels a").map(_.getText.trim)

      new PullreqDto(url, taskKey, taskName, labels)
    })
    .filter(p => {
      !Settings.pullreq.ignoredTaskKeyList.contains(p.taskKey)
    })

  prList.foreach(p => {
    println(p.taskKey)
  })

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
      case Some(v) => v.toList
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
      case Some(v) => v.toList
      case None => List.empty
    }
  }
}

class PullreqDto(
    val url: String,
    val taskKey: String,
    val taskName: String,
    val labelList: List[String]) {

  //val status = ""

}
