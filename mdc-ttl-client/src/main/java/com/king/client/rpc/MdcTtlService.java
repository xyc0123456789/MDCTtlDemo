package com.king.client.rpc;

import com.king.model.MyPerson;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(name = "mdc-ttl-server")
public interface MdcTtlService {
    @PostMapping("/mdc-ttl-server/test2")
    String test2( MyPerson person);

    @GetMapping("/mdc-ttl-server/test1/{id}")
    String test1(@PathVariable String id);
}
