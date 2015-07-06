package org.nita.sparkExample.tianchi

import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf, SparkContext}
/**
 * Created by havstack on 4/9/15.
 */
object BuyCar {

  def main(args:Array[String])={

    // set up environment

    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .setMaster("local[5]")
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
      (user_id.toInt,info)}).groupByKey().cache()

    //ans.collect()
    val buycar=ans.map(a=>{
      val items=a._2
      var d = items.map(item => {
        var fields=item.split(",")
        //println(fields(1))
        (fields(1).toInt,fields(2),fields(3),fields(4),fields(5))
      })

      val behavior3=d.filter(x=> (x._2 == "3"))
      val behavior4=d.filter(x=> x._2 == "4")

      val result=behavior3.filter(x=>{
        var flag=true
        behavior4.foreach(y =>
          if(x._1 == y._1 && x._5 <= y._5) flag=false
        )
        flag
      }).filter(m=> {
        proMap.contains(m._1)
      })



//
//
//      println("user=>"+a._1)
//      println("==>"+behavior3.size)
//      println("==>"+result.size)

      val item_ids=result.map(t =>
        a._1+","+t._1
      )
      item_ids
    }).map(r=>{
      var str=""
      r.foreach(y=>str+=(y+"\n"))
      str.trim
    }).filter(m => {if(m.size==0) false else true})

    println(buycar.count)
    buycar.coalesce(1,true).saveAsTextFile("/home/havstack/ans")


    val mylines=buycar.toArray()



    //Thread.sleep(4000)

    sc.stop()
//    var num=0
//    val writer = new PrintWriter(new File("/home/havstack/test.txt" ))
//    mylines.foreach(a => {
//      a._2.foreach(b=> {
//        writer.write(a._1 + "," + b + "\n")
//        num += 1
//      }
//      )
//    }
//    )
//
//
//    println("number ="+num)
  }
}
