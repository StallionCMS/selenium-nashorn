package io.stallion.selenium;

import jdk.nashorn.api.scripting.JSObject;

import java.util.ArrayList;
import java.util.List;



public class Suite {
    private JSObject before = null;
    private JSObject after = null;
    private List<JSObject> tests = new ArrayList<>();

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

    public List<JSObject> getTests() {
        return tests;
    }

    public Suite setTests(List<JSObject> tests) {
        this.tests = tests;
        return this;
    }
}
