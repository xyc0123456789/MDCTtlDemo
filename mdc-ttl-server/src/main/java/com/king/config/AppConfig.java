package com.king.config;

import com.google.common.eventbus.EventBus;
import com.king.model.MyPerson;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class AppConfig {


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
