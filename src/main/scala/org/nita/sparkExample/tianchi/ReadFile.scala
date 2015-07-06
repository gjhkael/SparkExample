package org.nita.sparkExample.tianchi

import scala.io.Source

/**
 * Created by havstack on 3/31/15.
 */
object ReadFile {

  def main(args:Array[String])={
    val source = Source.fromFile("/home/havstack/Downloads/tianchi_mobile_recommend_train_item.csv", "UTF-8")
    val lineIterator = source.getLines
    lineIterator.foreach(a => {
      var line=a.split(",")
      println(line(0)+":"+line(1)+":"+line(2))
    }
    )


  }
}
