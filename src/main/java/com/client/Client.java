package com.client;


import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;


import java.io.IOException;


public class Client {
    // 创建ES客户端
    private  RestHighLevelClient client;

    public Client(){ //默认
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );
    }

    public Client(String host,int port, String scheme){
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(host,port, scheme)));
    }

    public RestHighLevelClient getClient(){
        return client;
    }

    // 关闭ES客户端
    public void stopClient() throws IOException {
        client.close();
    }
    
}
