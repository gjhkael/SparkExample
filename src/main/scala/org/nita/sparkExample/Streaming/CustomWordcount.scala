package org.nita.kael

//import org.apache.spark.examples.streaming.StreamingExamples
import java.io.File

import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.{Seconds, StreamingContext}
object CustomWordcount {

  def main(args: Array[String]) {
    if (args.length < 1) {
      System.err.println("Usage: NetworkWordCount <master> <hostname> <port>\n" +
        "In local mode, <master> should be 'local[n]' with n > 1")
      System.exit(1)
    }
   // StreamingExamples.setStreamingLogLevels()
    var ssc = createStreamingContext(args(0))
    ssc.start()
    val f = new File("/home/havstack/test.txt")
    var modify = f.lastModified()
    
    while (true) {
      if ((f.lastModified() - modify) > 0) {
        modify = f.lastModified()
        /*ssc.stop()
        ssc = createStreamingContext()
        ssc.start()
        val t =ssc.receiverStream(new CustomReceiver("localhost", 9996))
        val words = t.flatMap(_.split(" "))
        val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)
        wordCounts.foreachRDD(rdd => rdd.foreach(record => println(record._1 + " " + record._2)))
        println(ssc)*/
        //StreamAction.add(ssc)



      } else {
        Thread.sleep(10000)
      }
    }
    
    println("ssc start !!")
    ssc.awaitTermination()
  }
  
  def createStreamingContext(str:String) ={
      //StreamingExamples.setStreamingLogLevels()
      val sparkConf = new SparkConf().setAppName("helloworld").setMaster(str)
      var ssc = new StreamingContext(sparkConf, Seconds(1))

      val loadnode = scala.xml.XML.loadFile("/home/havstack/eclipse/workspace/wordcount/hostAndIp.xml")
      var HostName: Seq[String] = null
      var Port: Seq[String] = null
      loadnode match {
        case <catalog>{ therms @ _* }</catalog> =>
          HostName = for (therm @ <HostPort>{ _* }</HostPort> <- therms) yield (therm \ "HostName").text
          Port = for (therm @ <HostPort>{ _* }</HostPort> <- therms) yield (therm \ "Port").text
      }
      HostName.foreach(println)
      Port.foreach(println)
      (0 until Port.length).map{
        i=>ssc.receiverStream(new CustomReceiver(HostName(i), Port(i).toInt)).flatMap(_.split(" ")).
        map(x => (x, 1)).reduceByKey(_ + _).foreachRDD(rdd => rdd.foreach(record => println(record._1 + " " + record._2)))
      }

    /*val fis = (0 until Port.length).map { i => ssc.receiverStream(new CustomReceiver(HostName(i), Port(i).toInt)) }
    val unionStreams = ssc.union(fis)
    //val customReceiverStream = ssc.receiverStream(new CustomReceiver(args(1), args(2).toInt))
    val words = unionStreams.flatMap(_.split(" "))
    val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)
    wordCounts.foreachRDD(rdd => rdd.foreach(record => println(record._1 + " " + record._2)))*/
    ssc
  }
}