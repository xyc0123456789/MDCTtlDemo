package com.king.customer.controller;

import com.alibaba.nacos.client.utils.JSONUtils;
import com.king.client.rpc.MdcTtlService;
import com.king.model.MyPerson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;


@RestController
@Slf4j
public class TestRpc {

    @Autowired
    private MdcTtlService mdcTtlService;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/rpc1")
    @ResponseBody
    public String testRpc1(){
        MyPerson rpc = new MyPerson("rpc1");
        log.info("rpc1->>test2");
        return mdcTtlService.test2(rpc);
    }

    @GetMapping("/rpc2")
    @ResponseBody
    public String testRpc2() throws IOException {
        MyPerson rpc = new MyPerson("rpc2");
        log.info("rpc2->>test2");
        String reqUrl = "http://mdc-ttl-server/mdc-ttl-server/test2";
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(JSONUtils.serializeObject(rpc), requestHeaders);
        ResponseEntity<String> entity = restTemplate.postForEntity(reqUrl, requestEntity,String.class);
        log.info(entity.getBody());
        return entity.getBody();
    }

    @GetMapping("/rpc3")
    @ResponseBody
    public String testRpc3(){
        log.info("rpc3->>test1");
        return mdcTtlService.test1("rpc3");
    }
}
