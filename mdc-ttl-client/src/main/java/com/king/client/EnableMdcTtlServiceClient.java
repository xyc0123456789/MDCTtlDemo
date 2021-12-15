package com.king.client;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ClientConfig.class)
@Documented
@Inherited
public @interface EnableMdcTtlServiceClient {
}
