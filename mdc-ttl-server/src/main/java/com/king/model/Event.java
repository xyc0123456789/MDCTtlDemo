package com.king.model;

import lombok.Data;


@Data
public class Event {
    private String name;

    public Event(String name) {
        this.name = name;
    }
}
