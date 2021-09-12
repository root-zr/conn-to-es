package com.spark

import java.net.{URI, URLEncoder}

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.{BasicCredentialsProvider, HttpClients}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

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

    // 设置代理服务器地址和端口
    val client = new HttpClient() //这里可以设置用来连接的密码等信息



    // 使用 GET 方法 ，如果服务器需要通过 HTTPS 连接，那只需要将下面 URL 中的 http 换成 https
    val method = new GetMethod("http://127.0.0.1:9201/_cat/indices?v")
    client.executeMethod(method)
    //打印服务器返回的状态
    println(method.getStatusLine)
    //打印返回的信息
    println(method.getResponseBodyAsString)
    //释放连接
    method.releaseConnection()


    // TODO 关闭环境
    spark.stop()
  }


}
