package com.king.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;

import java.util.UUID;


public class FeignTrackInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String mdcTraceId = MDC.get("mdc_trace_id");
        if (mdcTraceId==null){
            mdcTraceId = UUID.randomUUID().toString().replaceAll("-","");
        }
        requestTemplate.header("mdc_trace_id", mdcTraceId);
    }
}
