package com.analysis.crypto

import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object CryptoSentiments {

  /** Our main function where the action happens */
  def processCryptoSentiments (ssc : StreamingContext){
    //setting up twitter
    Util.setupTwitter()

    // Get rid of log spam (should be called after the context is set up)
    Util.setupLogging()

    // Create a DStream from Twitter using our streaming context
    val tweets = TwitterUtils.createStream(ssc, None, Constants.filterWords)
      .filter(_.getLang == "en")
      .filter(t=> t.toString.length > 0)
//      .filter(t=> (null != t.getGeoLocation))

        val statuses = tweets.map(
          tw => (
           // tw.getText().contains(filterWords.toList),
            //if (filterWords.toList.exists(words => words.contains(tw.getText))) filterWords else null,
            //tw.getText.exists(filter.toList),
            extractWords(tw.getText(), Constants.filterWords),
            SentimentAnalysisUtils.detectSentiment(tw.getText()).toString,
//            tw.getRetweetCount
//            Option(tw.getGeoLocation).map(geo => { s"${geo.getLatitude},${geo.getLongitude}" })
//            tw.getHashtagEntities.map(_.getText)
            tw.getUser.getScreenName,
            tw.getUser.getLocation,
            tw.getText
//            tw.getGeoLocation.getLatitude,
//            tw.getGeoLocation.getLongitude




          ))

   // statuses.count().print()
//    statuses.print()

    val filStst = statuses.filter(t=> !(t._1.contains(","))).filter(t=> !(t._1.isEmpty))
//    val filStst = statuses.filter(t=> t._1.length==1)

    filStst.foreachRDD{ rdd =>
//      val spark = SparkSession.builder.config(rdd.sparkContext.getConf).getOrCreate()
      val spark = SparkSession.builder.config(rdd.sparkContext.getConf).enableHiveSupport().getOrCreate()
      import spark.implicits._
      val result = rdd.toDF("key_words", "sentiment", "user_name", "location", "tweet")
      result.write.mode("append").insertInto("crypto_sentiments")//.saveAsTable("crypto_sentiments")


//      result.show()
    }


//    statuses.foreachRDD{ rdd =>
//      val spark = SparkSession.builder.config(rdd.sparkContext.getConf).getOrCreate()
//      import spark.implicits._
//      val result = rdd.toDF("key_words", "sentiment", "user_name", "location", "tweet")
//      result.show()
//    }




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
//
////      val spark = SparkSession.builder.config(rdd.sparkContext.getConf).getOrCreate()//.enableHiveSupport().getOrCreate()
//    val spark = SparkSession.builder.config(rdd.sparkContext.getConf).enableHiveSupport().getOrCreate()
//      import spark.implicits._
//      val result = rdd.toDF("key_words", "sentiment", "user_name", "location", "tweet", "geoLoc")
////        val filterResult = result.filter("key_words is not null")
//      result.write.mode("overwrite").insertInto("crypto_sentiments")//.saveAsTable("crypto_sentiments")
////      filterResult.show()
////      result.show()
//
//    }




    ssc.checkpoint("checkpoint")
    ssc.start()
    ssc.awaitTermination()


  }
  def extractWords (tweet : String, wordArray: Array[String]): String = {
     //val tweet= "I love you bitcoin ripple ripple1 cypto1 #crypto"
    val splittedTweet = tweet.split(" ").toList.map(x => x.toUpperCase).map(_.replaceAll("#", ""))
    //val wordArray = Array("BitCoin", "Ripple", "crypto", "cardano", "IOTA", "litcoin", "cryptoCoin")
    val wordsList = wordArray.toList.map(_.toUpperCase)
    val s = wordsList.intersect(splittedTweet)
   // print("HOLA "+s.toString())  //mkString(", "))
//    if (s.isEmpty){
//
//    }

    s.mkString(", ")
//    s.mkString

  }




}
