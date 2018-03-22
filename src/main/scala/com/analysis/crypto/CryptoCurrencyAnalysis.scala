package com.analysis.crypto

import org.apache.log4j._
import org.apache.spark._
import org.apache.spark.streaming.{Seconds, StreamingContext}


/** Count up how many of each word appears in a book as simply as possible. */
object CryptoCurrencyAnalysis {

  /** Our main function where the action happens */
  def main(args: Array[String]) {

    val sparkConf = new SparkConf().setAppName("CryptoCurrencyAnalysis")  //.setMaster("local[*]")
    val ssc = new StreamingContext(sparkConf, Seconds(10))
//    ssc.checkpoint("checkpoint")
    //Query for popular hastags
//    val checkpointDir = "harsh" //args(0)
  //  val outputPathPopularHashtags =  args(0) //"/home/harsh/IdeaProjects/CryptoCurrencyAnalysis/Resources/output1" //args(1)
//    val twitterCredentials = args(1)
    PopularHashtags.processPopularHashtags(ssc)


  }

}