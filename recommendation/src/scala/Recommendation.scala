
import java.util.Map

import entity.Item
import external.TicketMasterAPI

import scala.collection.JavaConverters._
import org.apache.spark.sql._
object Recommendation extends TicketMasterAPI {
  case class Pair(id:String,name: String, score:Double)

  /**
    * Get Hamming Score of sets of keywords
    * @param events
    * @param keywords
    */
  def getSimilarity(keywordCount: Map[String,Integer], item: Item): Pair ={
    var score = 0
    val itemKeywords = item.getCategories
    val keywords = keywordCount.keySet().asScala.toList
    for (key <- keywords){
      if (itemKeywords.contains(key)){
        score  = score + keywordCount.getOrDefault(key,0);
      }
    }

    val res = score.toDouble/ itemKeywords.size()
    return new Pair(item.getItemId,item.getName,res)
  }

  def getAllScores(keywords: Map[String,Integer],events: java.util.List[Item]): List[Pair] = {
    val list = events.asScala.toList
    val similarities = list.map(x => getSimilarity(keywords,x))
    return similarities
  }


  def getRecommendedItems( cateCount : Map[String,Integer], lat:Double,lon:Double): List[String] ={
    val api : TicketMasterAPI = new TicketMasterAPI()
    val events = api.search(lat,lon,null)
    val scores = getAllScores(cateCount,events)
    val spark = SparkSession
      .builder
      .appName("SparkSQL")
      .master("local[*]")
      .getOrCreate()
    val lines = spark.sparkContext.parallelize(scores)
    val schema = lines.toDS().cache()
    schema.printSchema()
    val list = schema.select("id")
      .filter(schema("score") > 0.3)
      .orderBy("score")
      .limit(10).collect.map(x => (x.getString(0))).toList

    return list
  }

  def main(args: Array[String]): Unit = {
    val map : java.util.HashMap[String,Integer] = new java.util.HashMap[String,Integer]();
    map.put("Group",1)
    map.put("Sports",2)
    val list = getRecommendedItems(map,29.682684, -95.295410)
    list.foreach(println)
  }


}
