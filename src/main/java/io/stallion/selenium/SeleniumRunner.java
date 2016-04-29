package io.stallion.selenium;

import jdk.nashorn.api.scripting.JSObject;

import java.util.ArrayList;
import java.util.List;



public class SeleniumRunner {
    private List<SeleniumTest> tests = new ArrayList<>();
    private List<Suite> suites = new ArrayList<>();
    private SeleniumContext context;


    public SeleniumRunner(SeleniumContext context) {
        this.context = context;
    }

    public SeleniumRunner addSuite(JSObject o) {
        Suite suite = new Suite();
        suites.add(suite);
        for(String name: o.keySet()) {
            if ("before".equals(name)) {
                suite.setBefore((JSObject)o.getMember(name));
                continue;
            } else if ("after".equals(name)) {
                suite.setAfter((JSObject)o.getMember(name));
                continue;
            } else if (!name.startsWith("test")) {
                continue;
            }
            JSObject object = (JSObject)o.getMember(name);
            if (!object.isFunction()) {
                continue;
            }
            suite.getTests().add(object);
        }
        return this;
    }

    public void run() {
        for (Suite suite: suites) {
            if (suite.getBefore() != null) {
                suite.getBefore().call(this, context.getDriver(), context.getHelper());
            }
            for (JSObject func :suite.getTests()) {
                func.call(this, context.getDriver(), context.getHelper());
            }
            if (suite.getAfter() != null) {
                suite.getAfter().call(this, context.getDriver(), context.getHelper());
            }
        }
    }
}
