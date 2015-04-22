package org.nita.sparksql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.sql.DriverManager
import java.sql.SQLException
import scala.reflect.io.Streamable.Bytes
import org.apache.spark.rdd.JdbcRDD
import java.sql.ResultSet

object SparkSqlFromMysql {
	def main(args:Array[String]){
	  val sparkConf = new SparkConf().setAppName("SparkSqlFromMysql")
      sparkConf.setMaster(args(0))
      val sc=new SparkContext(sparkConf)
      val rdd = new JdbcRDD(
          sc,
          () => {DriverManager.getConnection("jdbc:mysql://192.168.1.99:3306/videoMonitor","root", "111111")},
          "SELECT count(name) FROM picture_info WHERE ?<=serial AND serial<=? AND name='mxf'",
          1,300000,3,
          (r:ResultSet)=>{r.getString(1)}).cache()
          rdd.map(x=>println(x)).count
	}
}