package io.stallion.selenium;

import jdk.nashorn.api.scripting.JSObject;

public class TestFunction {
    private String name;
    private JSObject func;

    public String getName() {
        return name;
    }

    public TestFunction setName(String name) {
        this.name = name;
        return this;
    }

    public JSObject getFunc() {
        return func;
    }

    public TestFunction setFunc(JSObject func) {
        this.func = func;
        return this;
    }
}
