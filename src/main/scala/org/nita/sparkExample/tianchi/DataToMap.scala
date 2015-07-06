package org.nita.sparkExample.tianchi

import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by havstack on 4/8/15.
 */
object DataToMap {
  def main(args:Array[String])= {
    // set up environment

    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .setMaster("local[4]")
    //.setMaster(args(0))
    //.setJars(List("/home/havstack/ideapro/alitianchi/out/artifacts/alitianchi_jar/alitianchi.jar"))
    val sc = new SparkContext(conf)


    val productText = sc.textFile("/home/havstack/tianchi/tianchi_mobile_recommend_train_item.csv")

    val producters = productText.flatMap(line => line.split('\n')).map(l => {

      val (item_id, info) = l.toString.splitAt(l.toString.indexOf(','))
      (item_id.toInt -> info)
    }).collect()

    var proMap = producters.toMap
    println(proMap.contains(104053709))
  }
}
