package io.stallion.selenium;

import jdk.nashorn.api.scripting.JSObject;

import java.util.ArrayList;
import java.util.List;



public class Suite {
    private JSObject before = null;
    private JSObject after = null;
    private JSObject beforeSuite = null;
    private JSObject afterSuite = null;

    private List<TestFunction> tests = new ArrayList<>();
    private String name = "";

    public JSObject getBefore() {
        return before;
    }

    public Suite setBefore(JSObject before) {
        this.before = before;
        return this;
    }

    public JSObject getAfter() {
        return after;
    }

    public Suite setAfter(JSObject after) {
        this.after = after;
        return this;
    }

    public List<TestFunction> getTests() {
        return tests;
    }

    public Suite setTests(List<TestFunction> tests) {
        this.tests = tests;
        return this;
    }

    public String getName() {
        return name;
    }

    public Suite setName(String name) {
        this.name = name;
        return this;
    }

    public JSObject getBeforeSuite() {
        return beforeSuite;
    }

    public Suite setBeforeSuite(JSObject beforeSuite) {
        this.beforeSuite = beforeSuite;
        return this;
    }

    public JSObject getAfterSuite() {
        return afterSuite;
    }

    public Suite setAfterSuite(JSObject afterSuite) {
        this.afterSuite = afterSuite;
        return this;
    }
}
