package com.king.customer.intercepter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Component
@Slf4j
public class RestTrackInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        String mdcTraceId = headers.getFirst("mdc_trace_id");
        if (mdcTraceId==null){
            mdcTraceId = MDC.get("mdc_trace_id");
            if (mdcTraceId==null){
                mdcTraceId = UUID.randomUUID().toString().replaceAll("-","");
            }
        }
        String tmp = mdcTraceId;
        log.info("mdcTraceId:{}",mdcTraceId);
        MDC.put("mdc_trace_id",tmp);
        // 请求头传递参数:trace-id
        headers.add("mdc_trace_id", mdcTraceId);
        // 保证请求继续被执行
        return execution.execute(request, body);
    }
}
