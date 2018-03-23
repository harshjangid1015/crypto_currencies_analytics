package com.analysis.crypto

//import org.apache.spark
//import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
//import org.apache.spark.streaming.StreamingContext._

/** Listens to a stream of Tweets and keeps track of the most popular
  *  hashtags over a 5 minute window.
  */
object PopularHashtags {

  /** Makes sure only ERROR messages get logged to avoid log spam. */
  def setupLogging() = {
    import org.apache.log4j.{Level, Logger}
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.OFF)
  }

//  /** Configures Twitter service credentials using twiter.txt in the main workspace directory */
//  def setupTwitter(twitterCredentialsPath: String) = {
//    import scala.io.Source
//    for (line <- Source.fromFile(twitterCredentialsPath).getLines) {
//      val fields = line.split(" ")
//      if (fields.length == 2) {
//        System.setProperty("twitter4j.oauth." + fields(0), fields(1))
//      }
//    }
//  }
//

  /** Configures Twitter service credentials using Constants.scala file*/
  def setupTwitter() = {

        System.setProperty("twitter4j.oauth.consumerKey", Constants.consumerKey)
        System.setProperty("twitter4j.oauth.consumerSecret", Constants.consumerSecret)
        System.setProperty("twitter4j.oauth.accessToken", Constants.accessToken)
        System.setProperty("twitter4j.oauth.accessTokenSecret", Constants.accessTokenSecret)

  }

  /** Our main function where the action happens */
  def processPopularHashtags (ssc : StreamingContext) {

//    val CHECKPOINT_DIR = checkpointDirName

    // Configure Twitter credentials using twitter.txt
    setupTwitter()

    // Set up a Spark streaming context named "PopularHashtags" that runs locally using
    // all CPU cores and one-second batches of data
   // val ssc = new StreamingContext("local[*]", "PopularHashtags", Seconds(1))

    // Get rid of log spam (should be called after the context is set up)
    setupLogging()

    // filter based on Crypto keywords
    val filters = Array("BitCoin", "Ripple", "crypto", "cardano", "IOTA", "litcoin", "bit", "cryptoCoin")
    // Create a DStream from Twitter using our streaming context
    val tweets = TwitterUtils.createStream(ssc, None, filters)

    // Now extract the text of each status update into DStreams using map()
    val statuses = tweets.map(status => status.getText())

    // Blow out each word into a new DStream
    val tweetwords = statuses.flatMap(tweetText => tweetText.split(" "))

    // Now eliminate anything that's not a hashtag
    val hashtags = tweetwords.filter(word => word.startsWith("#"))

    // Map each hashtag to a key/value pair of (hashtag, 1) so we can count them up by adding up the values
    val hashtagKeyValues = hashtags.map(hashtag => (hashtag, 1))

    // Now count them up over a 5 minute window sliding every one second
    val hashtagCounts = hashtagKeyValues.reduceByKeyAndWindow((x, y) => x + y, (x, y) => x - y, Seconds(300), Seconds(10))
    //  You will often see this written in the following shorthand:
    //val hashtagCounts = hashtagKeyValues.reduceByKeyAndWindow( _ + _, _ -_, Seconds(300), Seconds(1))

    // Sort the results by the count values
    val sortedResults = hashtagCounts.transform(rdd => rdd.sortBy(x => x._2, false))

    // Print the top 10
    sortedResults.print
   // sortedResults.saveAsTextFiles(outputDirPath)
    sortedResults.foreachRDD { rdd =>
      val spark = SparkSession.builder.config(rdd.sparkContext.getConf).enableHiveSupport().getOrCreate()
      import spark.implicits._
      // Convert RDD[String] to DataFrame
      val popularHashtagsDataFrame = rdd.toDF("popular_hashtag_name", "counts")
      popularHashtagsDataFrame.write.mode("overwrite").saveAsTable("popular_hashtags")
    }

    // Set a checkpoint directory, and kick it all off
    // I could watch this all day!
//    ssc.checkpoint("/home/harsh/IdeaProjects/CryptoCurrencyAnalysis/Resources/checkpoint")
    ssc.checkpoint("checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }
}


//words.foreachRDD { rdd =>
//
//// Get the singleton instance of SparkSession
//val spark = SparkSession.builder.config(rdd.sparkContext.getConf).getOrCreate()
//import spark.implicits._
//
//// Convert RDD[String] to DataFrame
//val wordsDataFrame = rdd.toDF("word")
//
//// Create a temporary view
//wordsDataFrame.createOrReplaceTempView("words")
//
//// Do word count on DataFrame using SQL and print it
//val wordCountsDataFrame =
//spark.sql("select word, count(*) as total from words group by word")
//wordCountsDataFrame.show()