package com.spark


import java.net.URI

import org.apache.commons.httpclient.methods.GetMethod
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{BasicCredentialsProvider, HttpClients}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.spark
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.util.StringUtils
import org.apache.spark.sql.{DataFrame, SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import sun.net.www.http.HttpClient

object SparkTest {

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
    val provider = new BasicCredentialsProvider()

    val connManager = new PoolingHttpClientConnectionManager()
    val client = HttpClients.custom.setConnectionManager(connManager) //connManager可以用来设置跨域的问题
      .setDefaultCredentialsProvider(provider) //proviser可以用来设置用户名和密码
      .build()


    //val stringEntity = new StringEntity()
    val httpPost = new HttpPost();
        httpPost.addHeader("Content-Type", "application/json")
    //httpPost.setEntity()

    val esNodes = "localhost:9201"
    val esDefaultPort = "9201"

    def getHttpReponse(esNodes: String): Unit = {
      val node = esNodes.split(",").head
      val Array(host, port) = (if (node.contains(":")) node else s"${node.trim}:${esDefaultPort}").split(":")
      val esNode = s"${host}:${port}"
      val searchUrl = s"http://${esNode}/_search"
      httpPost.setURI(URI.create(searchUrl))
     // httpPost.setURI(URI.create("http://127.0.0.1:9201/_cat/indices?v"))

      println(searchUrl)


      val httpResponse = try {
        client.execute(httpPost)
      } catch {
        case e: Exception => null
      }

      println(httpResponse)
    }

    val httpResponse = getHttpReponse(esNodes)
    println(httpResponse)





    // TODO 关闭环境
    spark.stop()
  }


}
