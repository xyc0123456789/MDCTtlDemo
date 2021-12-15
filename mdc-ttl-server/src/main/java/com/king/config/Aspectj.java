package com.king.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;


@Aspect
@Slf4j
@Component
public class Aspectj {

    @Pointcut("execution(* com.king.controller..*.*(..))")
    public void setLog() {
    }

    @Around("setLog()")
    public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {


        String ori = MDC.get("mdc_trace_id");
        // 生成 id
        String requestId;
        // 设置 id 到当前线程
        if (ori==null){
            requestId = UUID.randomUUID().toString().replace("-", "");
        }else {
            requestId = ori;
        }
//        Object[] args = joinPoint.getArgs();
//        Object arg =  args[0];
//        Method method = arg.getClass().getMethod("getName", null);
//        Object name = method.invoke(arg, null);
//        log.info("name:{}",name);
        // 在拦截器中将对应的requestId放到MDC中
        MDC.put("mdc_trace_id", requestId);
        log.info("ori:{}",ori);
        log.info("requestId:{}",requestId);
        //切面执行
        Object result=null;
        try {
            result = joinPoint.proceed();
        }catch (Throwable e){
            log.error("切面执行异常",e);
        }
        // 移除 id
        MDC.remove("mdc_trace_id");

        return result;
    }
}
