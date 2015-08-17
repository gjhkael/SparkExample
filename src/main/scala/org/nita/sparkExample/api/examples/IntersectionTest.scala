package org.nita.sparkExample.api.examples

import org.apache.spark.SparkContext

object IntersectionTest {

   def main(args: Array[String]) {
    
    val sc = new SparkContext("local", "Intersection Test") 
    val a = sc.parallelize(List(1, 2, 3, 3, 4, 5), 3)
    val b = sc.parallelize(List(1, 2, 5, 6), 2)
    //val c = sc.parallelize(List(1, 2, 3), 1)
    
    val r = a.intersection(b)
	//r.foreachWith(i => i)((x, i) => println("[PartitionIndex " + i + "] " + x))
	
	println(r.toDebugString)
	// [PartitionIndex 1] 1
	// [PartitionIndex 2] 5
	// [PartitionIndex 2] 2
  }
}