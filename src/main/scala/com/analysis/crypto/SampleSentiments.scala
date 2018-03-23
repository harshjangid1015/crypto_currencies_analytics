package com.analysis.crypto
//SampleSentiments

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}


object SampleSentiments {


  /** Makes sure only ERROR messages get logged to avoid log spam. */
  def setupLogging() = {
    import org.apache.log4j.{Level, Logger}
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.OFF)
  }

  /** Configures Twitter service credentials using Constants.scala file */
  def setupTwitter() = {

    System.setProperty("twitter4j.oauth.consumerKey", Constants.consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", Constants.consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", Constants.accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", Constants.accessTokenSecret)

  }

  /** Our main function where the action happens */
  def processCryptoSentiments(ssc: StreamingContext) {
    //setting up twitter
    setupTwitter()

    // Get rid of log spam (should be called after the context is set up)
    setupLogging()

    //filter based on Crpoto keywords
    val filter = Array("BitCoin", "Ripple", "crypto", "cardano", "IOTA", "litcoin", "bit", "cryptoCoin")

    // Create a DStream from Twitter using our streaming context
    val tweets = TwitterUtils.createStream(ssc, None, filter)
    //    val senti = SentimentAnalysisUtils.detectSentiment(tweets.toString)

    tweets.print()

    tweets.foreachRDD { (rdd, time) =>
      rdd.map(t => {
        Map(
          "user" -> t.getUser.getScreenName,
          "created_at" -> t.getCreatedAt.toInstant.toString,
          "location" -> Option(t.getGeoLocation).map(geo => {
            s"${geo.getLatitude},${geo.getLongitude}"
          }),
          "text" -> t.getText,
          "hashtags" -> t.getHashtagEntities.map(_.getText),
          "retweet" -> t.getRetweetCount,
         // "language" -> detectLanguage(t.getText),
          "sentiment" -> SentimentAnalysisUtils.detectSentiment(t.getText).toString
        )
      }).saveAsTextFile("/user/harsh/output/")
    }

  }
}
