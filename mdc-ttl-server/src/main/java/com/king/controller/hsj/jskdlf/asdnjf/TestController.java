package com.king.controller.hsj.jskdlf.asdnjf;

import com.google.common.eventbus.EventBus;
import com.king.model.Event;
import com.king.model.MyPerson;
import com.king.model.Student;
import com.king.other.ConstParameter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
public class TestController {
    @Autowired
    private EventBus eventBus;

    @GetMapping("/test1/{id}")
    @ResponseBody
    public String test1(@PathVariable String id){
        log.info("i'm in test1 ,id:{}",id);
        String mdc_trace_id = MDC.get("mdc_trace_id");
        eventBus.post(new Event(mdc_trace_id));
        return ConstParameter.person.say()+":"+id;
    }

    @PostMapping("test2")
    @ResponseBody
    public String test2(@RequestBody MyPerson person){
        log.info("test2, {}",person.getName());
        return "test2";
    }

    @PostMapping("test3")
    @ResponseBody
    public String test3(@RequestBody Student student){
        log.info("test3, {}",student.getName());
        for (int i=0;i<10;i++){
            int t = i;
            Thread thread = new Thread(()->{
                log.info(String.valueOf(t));
            });
            thread.start();
        }
        return "test3";
    }
}
