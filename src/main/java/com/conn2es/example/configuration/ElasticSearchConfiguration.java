package com.conn2es.example.configuration;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfiguration {
    @Bean
    public RestHighLevelClient restHighLevelClient(){

        UsernamePasswordCredentials usernamePasswordCredentials = new
                UsernamePasswordCredentials("elastic","123456");

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                usernamePasswordCredentials);

//        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
//
//        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connManager)
//                .setDefaultCredentialsProvider(credentialsProvider)
//                .build();

        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"))
                .setRequestConfigCallback(requestConfigBuilder -> {
                    requestConfigBuilder.setConnectTimeout(-1);
                    requestConfigBuilder.setSocketTimeout(-1);
                    requestConfigBuilder.setConnectionRequestTimeout(-1);
                    return requestConfigBuilder;
                }).setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.disableAuthCaching();
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                });

        return new RestHighLevelClient(builder);

    }
}
