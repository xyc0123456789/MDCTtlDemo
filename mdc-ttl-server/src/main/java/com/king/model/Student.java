package com.king.model;

import java.io.Serializable;


public class Student implements Serializable {
    private String name;

    public Student() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
