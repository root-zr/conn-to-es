package com.spark

import java.net.URI

import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{BasicCredentialsProvider, HttpClients}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.util.parsing.json.JSON

object SparkGetTest {

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



    val credentials = new UsernamePasswordCredentials("elastic","123456")

    val provider = new BasicCredentialsProvider()
    provider.setCredentials(AuthScope.ANY,credentials)

    val connManager = new PoolingHttpClientConnectionManager()
    val client = HttpClients.custom.setConnectionManager(connManager) //connManager可以用来设置跨域的问题
      .setDefaultCredentialsProvider(provider) //proviser可以用来设置用户名和密码
      .build()

   val httpGet = new HttpGet()

    val esNodes = "localhost:9200"
    val esDefaultPort = "9200"

    def getHttpReponse(esNodes: String): CloseableHttpResponse = {
      val node = esNodes.split(",").head
      val Array(host, port) = (if (node.contains(":")) node else s"${node.trim}:${esDefaultPort}").split(":")
      val esNode = s"${host}:${port}"
      val searchUrl = s"http://${esNode}/user/_count"
       httpGet.setURI(URI.create(searchUrl))

      println(searchUrl)


      val httpResponse = try {
        client.execute(httpGet)
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

    val res = getFromJsonByXpath(jsonOption,"count").toString.toFloat.toInt
    println(res)

    // TODO 关闭环境
    spark.stop()
  }


}
