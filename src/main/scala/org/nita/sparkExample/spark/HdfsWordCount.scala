package org.nita.sparkExample.spark
/**
 * Created by gjh on 3/17/15.
 */
import org.apache.spark._
import SparkContext._
object HdfsWordCount {
 def main(args: Array[String]) {
    if (args.length < 1) {
      System.err.println("Usage: HdfsTest <file>")
      System.exit(1)
    }
    val sparkConf = new SparkConf().setAppName("HdfsTest").setMaster("local[4]")
                                    .set("spark.driver.allowMultipleContexts","true")
                                   
    val sc = new SparkContext(sparkConf)

    val file = sc.textFile(args(0))       //如果是本地文件：file:///home/havstack/xxx  如果是HDFS：hdfs://slave2:9000/data/test/xxx

    val result = file.flatMap(_.split(" ")).map(x => (x, 1)).reduceByKey(_ + _).cache() //map reduce操作

    result.foreach{x => println(x._1 + " " + x._2)}  //打印word和及其数量

    val sorted=result.map{
      
      case(key,value)=>(value,key)   //对（key，value）进行反转（value，key）
      
    }.sortByKey(true,1)          //对value进行排序

    val topk=sorted.top(10);     //取top10的word
    
    topk.foreach(println)        //打印

    sc.stop()                    //停止spark job
  }
}