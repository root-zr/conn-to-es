package com.conn2es.example.spark


import java.net.URI

import org.apache.commons.httpclient.methods.GetMethod
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.HttpResponse
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.impl.client.{BasicCredentialsProvider, HttpClients}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils
import org.apache.spark
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.util.StringUtils
import org.apache.spark.sql.{DataFrame, SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import sun.net.www.http.HttpClient

import scala.util.parsing.json.JSON

object SparkPostTest {

  def main(args: Array[String]): Unit = {

    // TODO 准备环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("sparkSession")

    val spark = SparkSession
      .builder()
      .config(sparkConf)
      .enableHiveSupport()
      .getOrCreate()

    val csv_data = spark.read.option("head", "true")
      .option("delimiter", "|")
      .csv("datas/csv_data.csv")

    csv_data.show()


//    val rdd = csv_data.rdd
//    rdd.saveToEs("user")

    val credentials = new UsernamePasswordCredentials("elastic","123456")

    val provider = new BasicCredentialsProvider()
    provider.setCredentials(AuthScope.ANY,credentials)

    val connManager = new PoolingHttpClientConnectionManager()
    val client = HttpClients.custom.setConnectionManager(connManager) //connManager可以用来设置跨域的问题
      .setDefaultCredentialsProvider(provider) //proviser可以用来设置用户名和密码
      .build()

    val dsl =
      """
        |{
        |   "from":0,
        |   "size":0,
        |   "track_total_hits":true,
        |   "_source":{
        |     "includes":[
        |         "COUNT"
        |     ],
        |     "excludes":[]
        |   },
        |   "aggregations":{
        |       "COUNT(_id)":{
        |          "value_count":{
        |              "field":"_id"
        |          }
        |       }
        |   }
        |}
        |""".stripMargin



    val stringEntity = new StringEntity(dsl,"UTF-8")
    stringEntity.setContentEncoding("UTF-8")
    stringEntity.setContentType("application/json")


    val httpPost = new HttpPost();
        httpPost.addHeader("Content-Type", "application/json")
        httpPost.setEntity(stringEntity)

    val esNodes = "localhost:9200"
    val esDefaultPort = "9200"

    def getHttpReponse(esNodes: String): CloseableHttpResponse = {
      val node = esNodes.split(",").head
      val Array(host, port) = (if (node.contains(":")) node else s"${node.trim}:${esDefaultPort}").split(":")
      val esNode = s"${host}:${port}"
      val searchUrl = s"http://${esNode}/_count"
     // httpPost.setURI(URI.create(searchUrl))
      httpPost.setURI(URI.create("http://127.0.0.1:9200/user/_search"))

      println(searchUrl)


      val httpResponse = try {
        client.execute(httpPost)
      } catch {
        case e: Exception => null
      }

      httpResponse match {
        case x:CloseableHttpResponse if x.getStatusLine.getStatusCode == 200 => x
        case _=>getHttpReponse(if(esNodes.contains(",")) esNodes.substring(esNodes.indexOf(",") + 1 ) else "")
      }

      return httpResponse
    }

    val httpResponse = getHttpReponse(esNodes)
    println(httpResponse)




    val jsonOption = JSON.parseFull(EntityUtils.toString(httpResponse.getEntity(),"UTF-8"))
    EntityUtils.consume(httpResponse.getEntity())
    httpResponse.close()
    client.close()

    def regJson(json:Option[Any]): Map[String,Any] = json match {
      case Some(map:Map[String,Any]) => map
    }

    def getFromJsonByXpath(input:Option[Any],xPath:String):Any ={
      val paths = xPath.split("\\.")
      val json = regJson(input).get(paths(0))
      if(paths.length > 1)
        getFromJsonByXpath(json,xPath.substring(xPath.indexOf(".") + 1 ))
      else json.get

    }

    val res = getFromJsonByXpath(jsonOption,"hits.total.value").toString.toFloat.toInt
    println(res)

    // TODO 关闭环境
    spark.stop()
  }


}
