package com.spark

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SparkGetTest2 {

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



    def getResponse(url: String, header: String = null): String = {
      val httpClient = HttpClients.createDefault()    // 创建 client 实例
      val get = new HttpGet(url)    // 创建 get 实例

//      if (header != null) {   // 设置 header
//        val json = JSON.parseObject(header)
//        json.keySet().toArray.map(_.toString).foreach(key => get.setHeader(key, json.getString(key)))
//      }

      val response = httpClient.execute(get)    // 发送请求
      EntityUtils.toString(response.getEntity)    // 获取返回结果
    }

    val res = getResponse("http://127.0.0.1:9201/_cat/indices?v")
    println(res)

    // TODO 关闭环境
    spark.stop()
  }


}
