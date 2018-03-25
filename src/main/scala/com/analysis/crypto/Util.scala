package com.analysis.crypto

object Util {

  /** Makes sure only ERROR messages get logged to avoid log spam. */
  def setupLogging() = {
    import org.apache.log4j.{Level, Logger}
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.OFF)
  }

  /** Configures Twitter service credentials using Constants.scala file*/
  def setupTwitter() = {

    System.setProperty("twitter4j.oauth.consumerKey", Constants.consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", Constants.consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", Constants.accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", Constants.accessTokenSecret)

  }
}
