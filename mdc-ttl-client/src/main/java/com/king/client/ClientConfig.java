package com.king.client;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableFeignClients
@ComponentScan({"com.king.client"})
public class ClientConfig {
}
