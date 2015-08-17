package org.nita.sparkExample.spark
import org.apache.spark.{SparkConf, SparkContext}
object IntersectionTest {
  def main(args: Array[String]) {
      val sparkConf = new SparkConf().setAppName("IntersectionTest").setMaster("local[4]")
      .set("spark.driver.allowMultipleContexts", "true")   //忽略此参数，目前还不支持多个context对象
      val sc = new SparkContext(sparkConf)
      val a=sc.parallelize(List(1,2,3,4,5,6),3)       //3为partition的个数，此方法为创建RDD的其中一个方法
      //val a=sc.makeRDD(List(1,2,3,4,5,6),3)          //同上,只要是继承至Seq序列的都可以作为第一个参数来构造RDD
      
      val b=sc.parallelize(List(1,2,5,6), 2)         //2为partition的个数
      var r=a.intersection(b)          //求a和b的交集
      a.foreach(x=>println(x))          //将每个数打印出来
      a.foreachWith(i=>i)((x,i)=>println("[aIndex"+i+"]"+x))    //其中i为partition的编号，x为位于该partition的值
      b.foreach(x=>println(x))
      b.foreachWith(i=>i)((x,i)=>println("[aIndex"+i+"]"+x))
      r.foreach(x=>println(x))  
      r.foreachWith(i=>i)((x,i)=>println("[aIndex"+i+"]"+x)) 
      println(r.toDebugString)    //将r的RDD调用过程打印出来
      sc.stop()
      
  }
}