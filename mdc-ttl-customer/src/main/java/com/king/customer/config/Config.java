package com.king.customer.config;


import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;



@Configuration
public class Config {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // 把自定义的RestTrackInterceptor添加到RestTemplate，这里可添加多个
//        restTemplate.setInterceptors(Collections.singletonList(restTrackInterceptor));
        return restTemplate;
    }

}
