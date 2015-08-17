package org.nita.sparkExample.tianchi

import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf, SparkContext}
/**
 * Created by havstack on 4/11/15.
 */
object GetDataByData {

  def main(args:Array[String])={

    //saveDataByDay("2014-12-18")

//    val datas=new Array[String](2)
//    datas(0)="2014-12-17"
//    datas(1)="2014-12-18"
//    saveDataByWithOutDays(datas)

    //userItemBehaviorSet()
    //buyDataOneDay("2014-12-18")



    val datas=new Array[String](2)
//    datas(0)="2014-11-17"
//    datas(1)="2014-11-18"
//    datas(2)="2014-11-19"
//    datas(3)="2014-11-20"
//    datas(0)="2014-11-21"
//    datas(0)="2014-12-17"
//    datas(1)="2014-12-18"
//    saveDataByWithDays(datas)

    //saveDataByDay("2014-11-24")

//    userItemBehaviorSetTest("F:/doo/result/20150411/with-2014-11-22/part-00000",
//      "F:/doo/result/20150412/with-2014-11-22-BehaviorSet")

    userItemBehaviorSet("F:/doo/result/20150411/with-2014-12-17/part-00000",
      "F:/doo/result/20150412/with-2014-12-17-BehaviorSet")
  }

  def saveDataByDay(data:String): Unit ={
    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .setMaster("local[4]")
    //.setMaster(args(0))
    //.setJars(List("/home/havstack/ideapro/alitianchi/out/artifacts/alitianchi_jar/alitianchi.jar"))
    val sc = new SparkContext(conf)


    val lines = sc.textFile("/home/havstack/tianchi/tianchi_mobile_recommend_train_user.csv")

    val ans=lines.flatMap(line => line.split('\n'))
      .map(l=>{
      val mySplits=l.toString.split(",")
      (mySplits(0),mySplits(1),mySplits(2),mySplits(3),mySplits(4),mySplits(5))}).filter(x=>x._6.startsWith(data))
      .map(x=> x._1+","+x._2+","+x._3+","+x._4+","+x._5+","+x._6)

    ans.coalesce(1,true).saveAsTextFile("/home/havstack/tianchi/result/20150411/"+data)
  }

  def saveDataByWithOutDays(datas:Array[String]): Unit ={
    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .setMaster("local[4]")
    //.setMaster(args(0))
    //.setJars(List("/home/havstack/ideapro/alitianchi/out/artifacts/alitianchi_jar/alitianchi.jar"))
    val sc = new SparkContext(conf)


    val lines = sc.textFile("/home/havstack/tianchi/tianchi_mobile_recommend_train_user.csv")

    val ans=lines.flatMap(line => line.split('\n'))
      .map(l=>{
      val mySplits=l.toString.split(",")
      (mySplits(0),mySplits(1),mySplits(2),mySplits(3),mySplits(4),mySplits(5))}).filter(x=> {
      var flag=false
      datas.foreach(
      data => flag=flag || x._6.startsWith(data)
      )
      !flag
    })
      .map(x=> x._1+","+x._2+","+x._3+","+x._4+","+x._5+","+x._6)

    var filename="without-"+datas(0)
    ans.coalesce(1,true).saveAsTextFile("/home/havstack/tianchi/result/20150411/"+filename)

  }
  def saveDataByWithDays(datas:Array[String]): Unit ={
    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .setMaster("local[4]")
    //.setMaster(args(0))
    //.setJars(List("/home/havstack/ideapro/alitianchi/out/artifacts/alitianchi_jar/alitianchi.jar"))
    val sc = new SparkContext(conf)


    val lines = sc.textFile("/home/havstack/tianchi/tianchi_mobile_recommend_train_user.csv")

    val ans=lines.flatMap(line => line.split('\n'))
      .map(l=>{
      val mySplits=l.toString.split(",")
      (mySplits(0),mySplits(1),mySplits(2),mySplits(3),mySplits(4),mySplits(5))}).filter(x=> {
      var flag=false
      datas.foreach(
        data => flag=flag || x._6.startsWith(data)
      )
      flag
    })
      .map(x=> x._1+","+x._2+","+x._3+","+x._4+","+x._5+","+x._6)

    var filename="with-"+datas(0)
    ans.coalesce(1,true).saveAsTextFile("/home/havstack/tianchi/result/20150411/"+filename)
  }


  def buyDataOneDay(data:String): Unit ={
    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .setMaster("local[4]")
    //.setMaster(args(0))
    //.setJars(List("/home/havstack/ideapro/alitianchi/out/artifacts/alitianchi_jar/alitianchi.jar"))
    val sc = new SparkContext(conf)


    val lines = sc.textFile("/home/havstack/tianchi/tianchi_mobile_recommend_train_user.csv")

    val ans=lines.flatMap(line => line.split('\n'))
      .map(l=>{
      val mySplits=l.toString.split(",")
      (mySplits(0),mySplits(1),mySplits(2),mySplits(3),mySplits(4),mySplits(5))}).filter(x=> x._6.startsWith(data) && x._3=="4")
      .map(x=> x._1+","+x._2+","+x._3+","+x._4+","+x._5+","+x._6)

    ans.coalesce(1,true).saveAsTextFile("/home/havstack/tianchi/result/20150411/"+data+"buy")

  }



  def userItemBehaviorSet(src:String,target:String): Unit ={
    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .setMaster("local[4]")
    //.setMaster(args(0))
    //.setJars(List("/home/havstack/ideapro/alitianchi/out/artifacts/alitianchi_jar/alitianchi.jar"))
    val sc = new SparkContext(conf)

    //"/home/havstack/tianchi/result/20150411/without-2014-12-17/part-00000"
    val lines = sc.textFile(src)
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
        val score=y._2(0).toDouble*0.1 + y._2(1).toDouble*0.2 + y._2(2).toDouble*0.5 -y._2(3).toDouble*5
        score+","+a._1+","+y._1+","+y._2(0)+"'"+y._2(1)+"'"+y._2(2)+"'"+y._2(3)+","+y._3
      })
      behavior
    }).map(r=>{
      var str=""
      r.foreach(z =>str+=(z + "\n"))
      str.trim
    }).filter(m => {if(m.size==0) false else true})


    val sortedData=buycar.flatMap(line => line.split('\n'))
      .map(l=>{
      val (user_id,info)=l.toString.splitAt(l.toString.indexOf(','))
      (user_id.toDouble,info)}).sortByKey(false,1).map(y=> y._1+y._2)

    buycar.coalesce(1,true).saveAsTextFile(target)

    sortedData.coalesce(1,true).saveAsTextFile(target+"sorted")
    val trainitem=sc.textFile("F:\\doo\\tianchi_mobile_recommend_train_item.csv")
    val items=trainitem.flatMap(line => line.split('\n'))
      .map(l=>{
      val mySplits=l.toString.split(",")
      (mySplits(0),mySplits(1)+","+mySplits(2))
    }).collect().toMap
    val sortedDataFilter=sortedData.filter(line=>{
      val splits=line.split(",")
      items.contains(splits(2))
    })
//    val rightData=sortedDataFilter.top(1000).filter(x=>{
//      var splits=x.split(",")
//      buyoneday.contains(splits(1)+":"+splits(2))
//    }).map(y=>y)


    val rightData=sortedDataFilter.top(1000)
    //rightData.coalesce(1,true).saveAsTextFile(target+"right")
    rightData.foreach(x=>{
      val splits=x.split(",")
      println(splits(1)+","+splits(2))
    })
    println(rightData.size)

  }



  def userItemBehaviorSetTest(src:String,target:String): Unit ={
    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .setMaster("local[4]")
    //.setMaster(args(0))
    //.setJars(List("/home/havstack/ideapro/alitianchi/out/artifacts/alitianchi_jar/alitianchi.jar"))
    val sc = new SparkContext(conf)

    //"/home/havstack/tianchi/result/20150411/without-2014-12-17/part-00000"
    val lines = sc.textFile(src)
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
        val score=y._2(0).toDouble*0.1 + y._2(1).toDouble*0.2 + y._2(2).toDouble*0.5 -y._2(3).toDouble*5
        score+","+a._1+","+y._1+","+y._2(0)+"'"+y._2(1)+"'"+y._2(2)+"'"+y._2(3)+","+y._3
      })
      behavior
    }).map(r=>{
      var str=""
      r.foreach(z =>str+=(z + "\n"))
      str.trim
    }).filter(m => {if(m.size==0) false else true})


    val sortedData=buycar.flatMap(line => line.split('\n'))
      .map(l=>{
      val (user_id,info)=l.toString.splitAt(l.toString.indexOf(','))
      (user_id.toDouble,info)}).sortByKey(false,1).map(y=> y._1+y._2)


    buycar.coalesce(1,true).saveAsTextFile(target)

    sortedData.coalesce(1,true).saveAsTextFile(target+"sorted")
    val buyonedays = sc.textFile("F:/doo/result/20150411/2014-11-24/part-00000")

    val trainitem=sc.textFile("F:\\doo\\tianchi_mobile_recommend_train_item.csv")
    val items=trainitem.flatMap(line => line.split('\n'))
      .map(l=>{
      val mySplits=l.toString.split(",")
      (mySplits(0),mySplits(1)+","+mySplits(2))
    }).collect().toMap
    val buyoneday=buyonedays.flatMap(line => line.split('\n'))
      .map(l=>{
      val mySplits=l.toString.split(",")
      (mySplits(0),mySplits(1),mySplits(2),mySplits(3),mySplits(4),mySplits(5))}).filter(x=> x._3=="4")
      .map(x=> (x._1+":"+x._2,","+x._3+","+x._4+","+x._5+","+x._6)).filter(y=>{
      val item=y._1.split(":")(1)
      items.contains(item)
    }).collect().toMap





    val sortedDataFilter=sortedData.filter(line=>{
      val splits=line.split(",")
      items.contains(splits(2))
    })



    val rightData=sortedDataFilter.top(1000).filter(x=>{
      var splits=x.split(",")
      buyoneday.contains(splits(1)+":"+splits(2))
    }).map(y=>y)
    //rightData.coalesce(1,true).saveAsTextFile(target+"right")

    rightData.foreach(println)
    println(rightData.size)
    println(sortedDataFilter.count())
  }



}
