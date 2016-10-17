package com.example.controller

import com.example.Settings
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory

class SlackController {
  private val session = SlackSessionFactory.createWebSocketSlackSession(Settings.slack.apiToken)

  /**
    * loan-pattern ?
    * http://xerial.org/scala-cookbook/recipes/2012/06/27/loan-pattern/
    *
    * @param msg
    */
  def sendMessage(msg: String): Unit = {
    session.connect()

    val channel = Option(session.findChannelByName(Settings.slack.postChannel))
    channel match {
      case Some(c) => session.sendMessage(c, msg)
      case None => throw new IllegalStateException(Settings.slack.postChannel + "is not found.")
      // TODO チャンネル名は設定から取ってるので、もっと親切に
    }

    // 注意：disconnectしないと常駐する
    session.disconnect()
  }
}
