package org.nita.MLlib
import java.util.Random
import org.apache.log4j.Logger
import org.apache.log4j.Level
import scala.io.Source
import scopt.OptionParser
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd._
import org.apache.spark.mllib.recommendation.{ALS, Rating, MatrixFactorizationModel}
import org.apache.spark.serializer.KryoRegistrator
import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoSerializer

object MovieRecommendation {
	class ALSRegistrator extends KryoRegistrator{
	  override def registerClasses(kryo:Kryo){
	    kryo.register(classOf[Rating])
	  }
	}
	case class Params(
      input: String = null,
      kryo: Boolean = false,
      numIterations: Int = 20,
      lambda: Double = 1.0,
      rank: Int = 10,
      implicitPrefs: Boolean = false)
      
    def main(args:Array[String]){
	    val defaultParams = Params()
	    println(defaultParams.input + defaultParams.kryo + defaultParams.numIterations + defaultParams.rank)
	    val parser = new OptionParser[Params]("MovieLensALS"){
	      head("MovieLensALS: an example app for ALS on MovieLens data.")
	      opt[Int]("rank")
	      	.text(s"rank, default: ${defaultParams.rank}}")
	      	.action((x,c)=>c.copy(numIterations=x))
	      opt[Int]("numIterations")
	        .text(s"number of iterations, default: ${defaultParams.numIterations}")
	        .action((x, c) => c.copy(numIterations = x))
	      opt[Double]("lambda")
	        .text(s"lambda (smoothing constant), default: ${defaultParams.lambda}")
	        .action((x, c) => c.copy(lambda = x))
	      opt[Boolean]("kryo")
	        .text(s"use Kryo serialization:${defaultParams.kryo}")
	        .action((x, c) => c.copy(kryo = x))
	      opt[Unit]("implicitPrefs")
	        .text("use implicit preference")
	        .action((_, c) => c.copy(implicitPrefs = true))
	      arg[String]("<input>")
	        .required()
	        .text("input paths to a MovieLens dataset of ratings")
	        .action((x, c) => c.copy(input = x))
	      note(
	        """
	          |For example, the following command runs this app on a synthetic dataset:
	          |
	          | bin/spark-submit --class org.apache.spark.examples.mllib.MovieLensALS \
	          |  examples/target/scala-*/spark-examples-*.jar \
	          |  --rank 5 --numIterations 20 --lambda 1.0 --kryo \
	          |  data/mllib/sample_movielens_data.txt
	        """.stripMargin)
	    }
	    
	    parser.parse(args, defaultParams).map { params =>
	      println(params)
	      run(params)
	    } getOrElse {
	      System.exit(1)
	    }
	}
	def run(params:Params){
		Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
		Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)
		
		val conf=new SparkConf().setAppName(s"MovieLensALS with $params")
		 .setMaster("local[4]")
		 
		if (params.kryo) {
			conf.set("spark.serializer", classOf[KryoSerializer].getName)
		  	.set("spark.kryo.registrator", classOf[ALSRegistrator].getName)
		  	.set("spark.kryoserializer.buffer.mb", "8")
		}
		
		val sc = new SparkContext(conf)  
		//load ratings file
		val ratings = sc.textFile("/home/havstack/Documents/sparkMltestdata/ml-10M/ratings.dat").map{
		  line => val fields = line.split("::")
		  // format: (timestamp % 10, Rating(userId, movieId, rating))
		  (fields(3).toLong%10,Rating(fields(0).toInt,fields(1).toInt,fields(2).toDouble))
		}
		//load movies file
		val movies = sc.textFile("/home/havstack/Documents/sparkMltestdata/ml-10M/movies.dat").map{
		  line => val fields = line.split("::")
		  // format: (movieId, movieName)
		  (fields(0).toInt,fields(1))
		}.collect.toMap
		// your code here
		val numRatings = ratings.count
		val numUsers = ratings.map(_._2.user).distinct.count
		val numMovies = ratings.map(_._2.product).distinct.count
		println("Got" + numRatings + "ratings from" + numUsers + "user on" + numMovies + "movies.")
		
		val mostRatedMovieIds = ratings.map(_._2.product)  // extract movie ids
									   .countByValue       // count ratings per movie
									   .toSeq              // convert map to Seq
									   .sortBy(- _._2)     // sort by rating count
									   .take(50)           // take 50 most rated
									   .map(_._1)          // get their ids
		val random = new Random(0)
		val selectedMovies = mostRatedMovieIds.filter(x => random.nextDouble()<0.2)
											  .map(x => (x,movies(x)))
											  .toSeq
		val myRatings = elicitateRatings(selectedMovies)
		val myRatingsRDD = sc.parallelize(myRatings)
		
		val numPartitions = 20
	    val training = ratings.filter(x => x._1 < 6)
	                          .values
	                          .union(myRatingsRDD)
	                          .repartition(numPartitions)
	                          .persist
	    val validation = ratings.filter(x => x._1 >= 6 && x._1 < 8)
	                            .values
	                            .repartition(numPartitions)
	                            .persist
	    val test = ratings.filter(x => x._1 >= 8).values.persist
	
	    val numTraining = training.count
	    val numValidation = validation.count
	    val numTest = test.count
	
	    println("Training: " + numTraining + ", validation: " + numValidation + ", test: " + numTest)
			
		val ranks = List(8, 12)
	    val lambdas = List(0.1, 10.0)
	    val numIters = List(10, 20)
	    var bestModel: Option[MatrixFactorizationModel] = None
	    var bestValidationRmse = Double.MaxValue
	    var bestRank = 0
	    var bestLambda = -1.0
	    var bestNumIter = -1
	    for (rank <- ranks; lambda <- lambdas; numIter <- numIters) {
	      val model = ALS.train(training, rank, numIter, lambda)
	      val validationRmse = computeRmse(model, validation, numValidation)
	      println("RMSE (validation) = " + validationRmse + " for the model trained with rank = "
	        + rank + ", lambda = " + lambda + ", and numIter = " + numIter + ".")
	      if (validationRmse < bestValidationRmse) {
	        bestModel = Some(model)
	        bestValidationRmse = validationRmse
	        bestRank = rank
	        bestLambda = lambda
	        bestNumIter = numIter
	      }
	    }
	
	    val testRmse = computeRmse(bestModel.get, test, numTest)
	
	    println("The best model was trained with rank = " + bestRank + " and lambda = " + bestLambda
	      + ", and numIter = " + bestNumIter + ", and its RMSE on the test set is " + testRmse + ".")
			
		
	    val myRatedMovieIds = myRatings.map(_.product).toSet
	    val candidates = sc.parallelize(movies.keys.filter(!myRatedMovieIds.contains(_)).toSeq)
	    val recommendations = bestModel.get
	                                   .predict(candidates.map((0, _)))
	                                   .collect
	                                   .sortBy(-_.rating)
	                                   .take(50)
	
	    var i = 1
	    println("Movies recommended for you:")
	    recommendations.foreach { r =>
	      println("%2d".format(i) + ": " + movies(r.product))
	      i += 1
	    }  
	      
	    val meanRating = training.union(validation).map(_.rating).mean
	    val baselineRmse = math.sqrt(test.map(x => (meanRating - x.rating) * (meanRating - x.rating))
                                     .reduce(_ + _) / numTest)
	    val improvement = (baselineRmse - testRmse) / baselineRmse * 100
	    println("The best model improves the baseline by " + "%1.2f".format(improvement) + "%.")  
		
	    
	    // clean up
		sc.stop()
	}
	 /** Compute RMSE (Root Mean Squared Error). */
	def computeRmse(model:MatrixFactorizationModel,data:RDD[Rating],n:Long)={
	  def mapPredictedRating(r: Double) = if (n>0) math.max(math.min(r, 1.0), 0.0) else r

	  val predictions: RDD[Rating] = model.predict(data.map(x => (x.user, x.product)))
    
      val predictionsAndRatings = predictions.map{ x =>
      	((x.user, x.product), mapPredictedRating(x.rating))
	  }.join(data.map(x => ((x.user, x.product), x.rating))).values
	  math.sqrt(predictionsAndRatings.map(x => (x._1 - x._2) * (x._1 - x._2)).mean())
	}
	/** Elicitate ratings from command-line. */
	def elicitateRatings(movies:Seq[(Int,String)])={
	   val prompt = "Please rate the following movie (1-5 (best), or 0 if not seen):"
		println(prompt)
		val ratings = movies.flatMap { x =>
			var rating: Option[Rating] = None
			var valid = false
			while (!valid) {
				print(x._2 + ": ")
				try {
					val r = Console.readInt
					if (r < 0 || r > 5) {
						println(prompt)
					} else {
						valid = true
							if (r > 0) {
								rating = Some(Rating(0, x._1, r))
							}
					}
				} catch {
					case e: Exception => println(prompt)
				}
			}
			rating match {
				case Some(r) => Iterator(r)
				case None => Iterator.empty
			}
	   }
	   if(ratings.isEmpty) {
			error("No rating provided!")
	   } else {
			ratings
	   }
		
	}
	
	
	
}