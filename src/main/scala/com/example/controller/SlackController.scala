package com.example.controller

import com.example.Settings
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory

class SlackController {
  private val session = SlackSessionFactory.createWebSocketSlackSession(Settings.slack.apiToken)
  session.connect()

  private val channel = Option(session.findChannelByName(Settings.slack.postChannel))
  channel match {
    case Some(c) => session.sendMessage(c, "Hello world")
    case None => throw new IllegalStateException(Settings.slack.postChannel + "is not found.")
  }

  // 注意：disconnectしないと常駐する
  session.disconnect()
}
