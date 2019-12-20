package com.rest.test;

public class HotKeyword {
    private String name;
    private Long count;

    public HotKeyword() {

    }

    public HotKeyword(String name, Long count) {
        this.name = name;
        this.count = count;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long value) {
        this.count = value;
    }
}
