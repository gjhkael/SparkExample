package org.nita.sparkExample.tianchi

import java.io.{File, PrintWriter}

import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf, SparkContext}
/**
 * Created by havstack on 4/7/15.
 */
object MySort {
  def main(args:Array[String])={


    if(args.length != 1){
      println("args error--------------------------")
    }
    // set up environment

    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .setMaster("local[4]")
      //.setMaster(args(0))
      //.setJars(List("/home/havstack/ideapro/alitianchi/out/artifacts/alitianchi_jar/alitianchi.jar"))
    val sc = new SparkContext(conf)


    val lines = sc.textFile("/home/havstack/tianchi/tianchi_mobile_recommend_train_user.csv")
    val productText=sc.textFile("/home/havstack/tianchi/tianchi_mobile_recommend_train_item.csv")


    val producters=productText.flatMap(line => line.split('\n')).map(l=>{

      val (item_id,info)=l.toString.splitAt(l.toString.indexOf(','))
      (item_id.toInt->info)
    }).collect()

    var proMap=producters.toMap
    proMap.foreach(println)

    val ans=lines.flatMap(line => line.split('\n'))
      .map(l=>{
      val (user_id,info)=l.toString.splitAt(l.toString.indexOf(','))
      (user_id.toInt,info)}).groupByKey()

    //ans.collect()
    val buycar=ans.map(a=>{
      val items=a._2
      var d = items.map(item => {
        var fields=item.split(",")
        //println(fields(1))
        (fields(1).toInt,fields(2),fields(3),fields(4),fields(5))
      })

      val behavior3=d.filter(x=> x._2 == "3")
      val behavior4=d.filter(x=> x._2 == "4")

      val result=behavior3.filter(x=>{
        var flag=true
        behavior4.foreach(y =>
          if(x._1 == y._1) flag=false
        )
        flag
      }).filter(m=>{
        proMap.contains(m._1)
      })





      println("user=>"+a._1)
      println("==>"+behavior3.size)
      println("==>"+result.size)

      val item_ids=result.map(t =>
      t._1
      ).toArray
      //result.foreach(println)
      if(item_ids.size > 0)
          a._1+","+item_ids(0)
      else ""
    })
    println(buycar.count)
    //buycar.saveAsTextFile("/home/havstack/ans")


    val mylines=buycar.toArray()
    val writer = new PrintWriter(new File("/home/havstack/test.txt" ))
    mylines.foreach(a =>
    if(a.size!=0) writer.write(a+"\n")
    )
    sc.stop()

  }
}
