package org.nita.sparksql

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

case class Person(name:String,age:Int)
object SparkSqlFromtxt {
  def main(args:Array[String]){
    val starttime=System.currentTimeMillis()
    val sparkConf = new SparkConf().setAppName("SparkSql")
    sparkConf.setMaster(args(0))
	val sc=new SparkContext(sparkConf)
	val sqlContext=new org.apache.spark.sql.SQLContext(sc)
    import sqlContext._
    import sqlContext.createSchemaRDD
    val people = sc.textFile("file:///home/havstack/Documents/sparkSQLtestdata/people.txt").map(_.split(",")).map(p => Person(p(0), p(1).trim.toInt))
    people.registerAsTable("people")
	val teenagers = sqlContext.sql("SELECT count(name) FROM people WHERE age >= 13 AND age <= 19")
	teenagers.map(t => "Name: " + t(0)).collect().foreach(println)
	val endtime=System.currentTimeMillis()
  }
}