
val scalaLangVersion = "2.11.8"
val jdkVersion = "1.8"
//val sparkVersion = "1.5.0-cdh5.6.0"
val sparkVersion = "2.2.0"
// Match the version of the other json4s libraries in the environment
val json4sVersion = "3.2.10"
// Needs to depend on the same version of kryo as is included in the environment
val kryoSerializersVersion = "0.24"
// Needed for running in YARN; look for version used in Spark
val derbyVersion = "10.11.1.1"

lazy val root = (project in file(".")).
  settings(
    name := "crypto_currencies_analytics",
    scalaVersion := scalaLangVersion,
    javacOptions ++= Seq("-source", jdkVersion, "-target", jdkVersion, "-Xlint"),
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion,// % "provided",
      "org.apache.spark" %% "spark-hive" % sparkVersion,// % "provided",
      "org.apache.spark" %% "spark-sql" % sparkVersion,// % "provided",
      "org.apache.spark" %% "spark-mllib" % sparkVersion,// % "provided",
      "org.apache.spark" %% "spark-streaming" % sparkVersion,// % "provided",
      //"org.apache.spark" % "spark-streaming-twitter_2.11" % "1.5.2"
      "org.apache.bahir" % "spark-streaming-twitter_2.11" % "2.0.1",
      "edu.stanford.nlp" % "stanford-corenlp" % "3.5.1",
      "edu.stanford.nlp" % "stanford-corenlp" % "3.5.1" classifier "models"

    ),
    assemblyJarName in assembly := s"${name.value}-${version.value}.jar",
    // don't need the scala language libraries since they're on the cluster
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
    resolvers +=
      "Cloudera Builds" at "https://repository.cloudera.com/artifactory/cloudera-repos"
    // Release steps are similar to the default, but skips pushing to a repository because that happens in Jenkins

  )
assemblyMergeStrategy in assembly := { case PathList("META-INF", xs @ _*) => MergeStrategy.discard case x => MergeStrategy.first }

//name := "CryptoCurrencyAnalysis"
//
//version := "0.1"
//
//scalaVersion := "2.11.8"
//
//val sparkVersion = "2.2.0"
//
//libraryDependencies ++= Seq(
//  "org.apache.spark" %% "spark-core" % sparkVersion,
//  "org.apache.spark" %% "spark-sql" % sparkVersion,
//  "org.apache.spark" %% "spark-mllib" % sparkVersion,
//  "org.apache.spark" %% "spark-streaming" % sparkVersion,
//  //"org.apache.spark" % "spark-streaming-twitter_2.11" % "1.5.2"
//  "org.apache.bahir" % "spark-streaming-twitter_2.11" % "2.0.1"
//)
