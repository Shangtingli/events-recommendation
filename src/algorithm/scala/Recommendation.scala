package algorithm.scala

import java.util.Map

import entity.Item
import external.TicketMasterAPI

import scala.collection.JavaConverters._
import org.apache.spark.sql._

import scala.collection.mutable.ListBuffer
object Recommendation extends TicketMasterAPI {
    case class Pair(index: Int,name: String, score:Double)

  /**
    * Get Hamming Score of sets of keywords
    * @param events
    * @param keywords
    */
    def getSimilarity(index: Int, keywordCount: Map[String,Integer], item: Item): Pair ={
          var score = 0
          val itemKeywords = item.getCategories
          val keywords = keywordCount.keySet().asScala.toList
          for (key <- keywords){
              if (itemKeywords.contains(key)){
                score  = score + keywordCount.getOrDefault(key,0);
              }
          }

          val res = score.toDouble/ itemKeywords.size()
          return new Pair(index,item.getName,res)
    }

    def getAllScores(keywords: Map[String,Integer],events: java.util.List[Item]): List[Pair] = {
          val list = events.asScala.toList

          val similarities = list.zipWithIndex.map(x => getSimilarity(x._2,keywords,x._1))

          return similarities
    }


    def getRecommendedItems( cateCount : Map[String,Integer], lat:Double,lon:Double): java.util.List[Item] ={
      val api : TicketMasterAPI = new TicketMasterAPI()
      val events = api.search(lat,lon,null)
      val scores = getAllScores(cateCount,events)
      val spark = SparkSession
        .builder
        .appName("SparkSQL")
        .master("local[*]")
        .getOrCreate()

      import spark.implicits._
      val lines = spark.sparkContext.parallelize(scores)
      val schema = lines.toDS().cache()
      schema.printSchema()
      val list = schema.select("index")
        .filter(schema("score") > 0.3)
        .orderBy("score")
        .limit(10).collect.map(x => (x.getInt(0))).toList

      var temp = new ListBuffer[Item]()
      for (i <- list){
         temp  += events.get(i)
      }


      val res = temp.toList
      res.foreach(println)
      return res.asJava
    }

    def main(args: Array[String]): Unit = {
        val map : java.util.HashMap[String,Integer] = new java.util.HashMap[String,Integer]();
        map.put("Group",1)
        map.put("Sports",2)
        val list = getRecommendedItems(map,29.682684, -95.295410)
    }


}
