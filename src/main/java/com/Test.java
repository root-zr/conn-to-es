package com;

import com.client.Client;
import com.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;


public class Test {
    public static void main(String[] args) throws Exception {

        // 创建ES客户端
        RestHighLevelClient client = new Client().getClient();

//        // 创建索引- 请求对象
//        CreateIndexRequest request = new CreateIndexRequest("user");
//
//        // 发送请求，获取响应
//        CreateIndexResponse response = client.indices().create(request,
//                RequestOptions.DEFAULT);
//        boolean acknowledged = response.isAcknowledged();
//
//        // 响应状态
//        System.out.println("操作状态= " + acknowledged);


        // 查询索引- 请求对象
        GetIndexRequest request2 = new GetIndexRequest("user");

        // 发送请求，获取响应
        GetIndexResponse response2 = client.indices().get(request2,
                RequestOptions.DEFAULT);
        System.out.println("aliases:" + response2.getAliases());
        System.out.println("mappings:" + response2.getMappings());
        System.out.println("settings:" + response2.getSettings());


        // 新增文档- 请求对象
        IndexRequest request3 = new IndexRequest();
        // 设置索引及唯一性标识
        request3.index("user").id("1001");
        // 创建数据对象
        User user = new User();
        user.setName("zhangsan");
        user.setAge(30);
        user.setSex("男");

        ObjectMapper objectMapper = new ObjectMapper();
        String productJson = objectMapper.writeValueAsString(user);
        // 添加文档数据，数据格式为JSON 格式
        request3.source(productJson, XContentType.JSON);
        // 客户端发送请求，获取响应对象
        IndexResponse response = client.index(request3, RequestOptions.DEFAULT);

        ////3.打印结果信息
        System.out.println("_index:" + response.getIndex());
        System.out.println("_id:" + response.getId());
        System.out.println("_result:" + response.getResult());


        // 关闭ES客户端
        client.close();
    }
}
