package com.king.config;

import com.google.common.eventbus.Subscribe;
import com.king.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class EventHandler {
    private ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5,5,0, TimeUnit.MINUTES,new LinkedBlockingQueue<>());

    @Subscribe
    public void handle(Event event){
        log.info("EVENT bus log :{}",event.getName());
//        Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
        for (int i=0;i<10;i++){
            int finalI = i;
            poolExecutor.execute(()->{
//                if (copyOfContextMap!=null) MDC.setContextMap(copyOfContextMap);
                log.info("i'm in executor,{}", finalI);
            });
        }
    }
}
