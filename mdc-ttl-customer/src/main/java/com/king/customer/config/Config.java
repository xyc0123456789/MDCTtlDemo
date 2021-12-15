package com.king.customer.config;

import com.king.customer.intercepter.RestTrackInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

import java.net.URI;
import java.util.Collections;
import java.util.Map;


@Configuration
public class Config {
    @Autowired
    private RestTrackInterceptor restTrackInterceptor;
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // 把自定义的RestTrackInterceptor添加到RestTemplate，这里可添加多个
        restTemplate.setInterceptors(Collections.singletonList(restTrackInterceptor));
        return restTemplate;
    }

}
