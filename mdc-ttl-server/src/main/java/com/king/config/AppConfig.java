package com.king.config;

import com.google.common.eventbus.EventBus;
import com.king.intercepter.HttpIntercepter;
import com.king.intercepter.RestTrackInterceptor;
import com.king.model.MyPerson;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.Collections;


@Configuration
public class AppConfig {

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


    @Bean
    public EventBus getEventBus(){
        return new EventBus();
    }

    @Bean
    public EventHandler getEventHandler(EventBus eventBus){
        EventHandler eventHandler= new EventHandler();
        eventBus.register(eventHandler);
        return eventHandler;
    }

    @Bean
    public MyPerson person(){
        return new MyPerson("i'm a man from config bean");
    }

}
