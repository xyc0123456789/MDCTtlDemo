package com.king.model;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;


@Slf4j
public class MyPerson implements Serializable {
    private String name;

    public MyPerson() {
    }

    public MyPerson(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String say(){
        log.info(name);
        return name;
    }
}
