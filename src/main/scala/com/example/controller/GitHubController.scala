package com.example.controller

import com.example.{Const, Settings}
import org.openqa.selenium.htmlunit.HtmlUnitDriver


class GitHubController {
  private val driver = new HtmlUnitDriver {
    get(Const.URL_GITHUB_LOGIN)
    findElementByCssSelector("#login_field").sendKeys(Settings.gitHub.id)
    findElementByCssSelector("#password").sendKeys(Settings.gitHub.password)
    findElementByCssSelector("[name=commit]").click()
    get(Const.URL_GITHUB_PULLREQ)

    if (getTitle.startsWith("Page not found")) {
      throw new IllegalArgumentException(s"ログインに失敗。id=${Settings.gitHub.id}")
    }
  }

  // get pullreq's
  private val prList = driver.findElementsByCssSelector(".js-issue-row .lh-condensed")

  println(prList)
}
