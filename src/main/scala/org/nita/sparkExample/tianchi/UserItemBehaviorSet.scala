package org.nita.sparkExample.tianchi

import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf, SparkContext}
/**
 * Created by havstack on 4/9/15.
 */
object UserItemBehaviorSet {

  def main(args:Array[String]): Unit ={
    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .setMaster("local[4]")
    //.setMaster(args(0))
    //.setJars(List("/home/havstack/ideapro/alitianchi/out/artifacts/alitianchi_jar/alitianchi.jar"))
    val sc = new SparkContext(conf)


    val lines = sc.textFile("/home/havstack/tianchi/test1.csv")
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
        (fields(1).toInt,fields(2).toInt,fields(3),fields(4),fields(5))
      }).groupBy(key=>key._1)

      val behavior=d.map(item => {


        var behaviorArr=new Array[Int](4)
        var item_category = -1
        item._2.foreach(i =>{
          item_category = i._4.toInt
          behaviorArr(i._2 - 1)+=1
        })

        (item._1,behaviorArr,item_category)

        //item._1+",("+behaviorArr(0)+"'"+behaviorArr(1)+"'"+behaviorArr(2)+"'"+behaviorArr(3)+"')"+","+item_category
      }).filter( x =>{
        //过滤掉用户只浏览一次，没有其他操作的数据
        if(x._2(0) <= 1 && x._2(1)+x._2(2)+x._2(3)==0) false
        else true
      }).map(y=>{
        a._1+","+y._1+","+y._2(0)+"'"+y._2(1)+"'"+y._2(2)+"'"+y._2(3)+","+y._3
      })



      behavior
//      val behavior3=d.filter(x=> x._2 == "3")
//      val behavior4=d.filter(x=> x._2 == "4")
//
//      val result=behavior3.filter(x=>{
//        var flag=true
//        behavior4.foreach(y =>
//          if(x._1 == y._1) flag=false
//        )
//        flag
//      })
//      //      println("user=>"+a._1)
//      //      println("==>"+behavior3.size)
//      //      println("==>"+result.size)
//
//      //      val item_ids=result.map(t =>
//      //        t._1
//      //      ).toArray
//      //      //result.foreach(println)
//      //      if(item_ids.size > 0)
//      //        a._1+","+item_ids(0)
//      //      else ""
//      if(behavior3.size!=0)
//        (behavior4.size.toDouble/behavior3.size.toDouble,(a._1,behavior3.size,behavior4.size))
//      else (0,(a._1,behavior3.size,behavior4.size))

    }).map(r=>{
      var str=""
      r.foreach(z =>str+=(z + "\n"))
      str.trim
    }).filter(m => {if(m.size==0) false else true})
    //buycar.sortByKey()
    //println(buycar.count)
  //  buycar.collect()


    buycar.coalesce(1,true).saveAsTextFile("/home/havstack/ans")


 //   val mylines=buycar.toArray()
//    val writer = new PrintWriter(new File("/home/havstack/tianchi/result/20150409/user_item_behavior_set.txt" ))
//    mylines.foreach(a =>
//      a._2.foreach(item =>
//        writer.write(a._1+","+item+"\n")
//      )
//
//    )
    sc.stop()
  }




}
