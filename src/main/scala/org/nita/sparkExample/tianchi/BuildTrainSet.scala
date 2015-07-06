package org.nita.sparkExample.tianchi

import org.apache.spark.{SparkConf, SparkContext}
/**
 * Created by havstack on 4/11/15.
 */
object BuildTrainSet {
  def main(args:Array[String]): Unit ={
    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .setMaster("local[4]")
    //.setMaster(args(0))
    //.setJars(List("/home/havstack/ideapro/alitianchi/out/artifacts/alitianchi_jar/alitianchi.jar"))
    val sc = new SparkContext(conf)

    //12-17号购买的产品
    val buy1217= sc.textFile("/home/havstack/tianchi/result/20150411/2014-12-17buy/part-00000")
    val buy=buy1217.flatMap(line => line.split('\n'))
      .map(l=>{

      val splits=l.split(",")
      (splits(0)+","+splits(1), splits(2)+","+splits(3)+","+splits(4)+","+splits(5) )}).collect().toMap

    //12-17前一个月的用户行为历史数据
    val histroyfile=sc.textFile("/home/havstack/ans/part-00000")
    val behavior=histroyfile.flatMap(line => line.split('\n'))
      .map(l=>{
      val splits=l.split(",")
      (splits(0)+","+splits(1), splits(2)+","+splits(3) )})


    val trainset=behavior.map(x =>{
      if( buy.contains(x._1)){
        x._1+","+x._2+",1"
      }else{
        x._1+","+x._2+",0"
      }
    })


    trainset.coalesce(1,true).saveAsTextFile("/home/havstack/tianchi/result/20150411/trainset")

    val da=trainset.filter(l=>{
      val splits=l.split(",")
      splits(4).contains("1")
    })

    println(da.count())
  }


}
