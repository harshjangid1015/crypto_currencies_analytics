package com.analysis.crypto

import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}


object CryptoSentiments {


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

  /** Our main function where the action happens */
  def processCryptoSentiments (ssc : StreamingContext){
    //setting up twitter
    setupTwitter()

    // Get rid of log spam (should be called after the context is set up)
    setupLogging()

    //filter based on Crpoto keywords
    val filterWords = Array("BitCoin", "Ripple", "crypto", "cardano", "IOTA", "litcoin", "cryptoCoin")

    // Create a DStream from Twitter using our streaming context
    val tweets = TwitterUtils.createStream(ssc, None, filterWords)
      .filter(_.getLang == "en")
      .filter(t=> t.toString.length > 0)

        val statuses = tweets.map(
          tw => (
           // tw.getText().contains(filterWords.toList),
            //if (filterWords.toList.exists(words => words.contains(tw.getText))) filterWords else null,
            //tw.getText.exists(filter.toList),
            tw.getText(),
            SentimentAnalysisUtils.detectSentiment(tw.getText()).toString))

    statuses.print()



        //Print the status
    //    statuses.print

//    val tweetsMaaped = tweets.foreachRDD { rdd =>
//      rdd.map(t => {
//        Map(
//          "user" -> t.getUser.getScreenName,
//          "created_at" -> t.getCreatedAt.toInstant.toString,
//          "location" -> Option(t.getGeoLocation).map(geo => {
//            s"${geo.getLatitude},${geo.getLongitude}"
//          }),
//          "text" -> t.getText,
//          "hashtags" -> t.getHashtagEntities.map(_.getText),
//          "retweet" -> t.getRetweetCount,
//          // "language" -> detectLanguage(t.getText),
//          "sentiment" -> SentimentAnalysisUtils.detectSentiment(t.getText).toString
//        )
//      })//.saveAsTextFile("/home/harsh/IdeaProjects/CryptoCurrencyAnalysis/target")
//    }

   // println(tweetsMaaped)
    // Now extract the text of each status update into DStreams using map()
//    val statuses = tweets.map(
//      tw => (tw.getText(),
//        SentimentAnalysisUtils.detectSentiment(tw.getText()).toString))
//
//    //Print the status
//    statuses.print

//    statuses.foreachRDD{ rdd =>
////      rdd.map(t =>{
////        Map(
////          "Status" -> t.,
////          "Sentiment" -> SentimentAnalysisUtils.detectSentiment(t.toString()).toString
////        )
////
////      })
//      val spark = SparkSession.builder.config(rdd.sparkContext.getConf).enableHiveSupport().getOrCreate()
//      import spark.implicits._
//      val result = rdd.toDF("hashtag", "status", "sentiment")
//     // result.write.mode("append").saveAsTable("crypto_sentiments")
//      result.show()
//
//    }
    ssc.checkpoint("checkpoint")
    ssc.start()
    ssc.awaitTermination()


  }


}
