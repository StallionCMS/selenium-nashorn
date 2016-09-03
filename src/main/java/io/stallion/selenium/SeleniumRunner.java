package io.stallion.selenium;

import jdk.nashorn.api.scripting.JSObject;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;



public class SeleniumRunner {
    public static boolean anyFailed = false;

    private List<SeleniumTest> tests = new ArrayList<>();
    private List<Suite> suites = new ArrayList<>();
    private SeleniumContext context;
    private String suiteName = "";
    private String testName = "";
    private List<String> succeeded = new ArrayList<>();
    private List<TestError> errors = new ArrayList<>();
    private int successCount = 0;

    public SeleniumRunner(SeleniumContext context, String tests) {
        if (tests != null) {
            if (tests.contains(":")) {
                String[] parts = tests.split(":");
                suiteName = parts[0];
                testName = parts[1];
            } else {
                suiteName = tests;
            }
        }
        this.context = context;
    }

    public SeleniumRunner addSuite(String suiteName, JSObject o) {
        Suite suite = new Suite().setName(suiteName);
        suites.add(suite);
        for(String name: o.keySet()) {
            if ("before".equals(name)) {
                suite.setBefore((JSObject)o.getMember(name));
                continue;
            } else if ("after".equals(name)) {
                suite.setAfter((JSObject) o.getMember(name));
                continue;
            } else if ("beforeSuite".equals(name)) {
                suite.setBeforeSuite((JSObject) o.getMember(name));
            } else if ("afterSuite".equals(name)) {
                suite.setBeforeSuite((JSObject) o.getMember(name));
            } else if (!name.startsWith("test")) {
                continue;
            }
            JSObject object = (JSObject)o.getMember(name);
            if (!object.isFunction()) {
                continue;
            }
            suite.getTests().add(new TestFunction().setFunc(object).setName(name));
        }
        return this;
    }

    public void run() {
        for (Suite suite: suites) {
            if (!"".equals(suiteName)) {
                if (!suiteName.equals(suite.getName())) {
                    continue;
                }
            }
            if (suite.getBeforeSuite() != null) {
                suite.getBeforeSuite().call(this, context.getDriver(), context.getHelper());
            }
            for (TestFunction testFunction :suite.getTests()) {
                if (!"".equals(testName)) {
                    if (!testName.equals(testFunction.getName())) {
                        continue;
                    }
                }
                if (suite.getBefore() != null) {
                    suite.getBefore().call(this, context.getDriver(), context.getHelper());
                }
                execFunction(suite.getName(), testFunction);
                if (suite.getAfter() != null) {
                    suite.getAfter().call(this, context.getDriver(), context.getHelper());
                }

            }
            if (suite.getAfterSuite() != null) {
                suite.getAfterSuite().call(this, context.getDriver(), context.getHelper());
            }
        }
        if (errors.size() > 0) {
            anyFailed = true;
        }
    }

    public void printResults() {
        System.out.println("\n\n");
        for (String name: succeeded) {
            System.out.println("Succeeded: " + name);
        }
        for (TestError error: errors) {
            System.err.println("Failed:    " + error.getSuite() + "." + error.getTest() + ": " + error.getMsg());
        }
        System.out.println("\n\n" + successCount + " tests succeeded, " + errors.size() + " failed.\n\n");
        if (errors.size() > 0) {
            System.err.println("\nExiting as failure.\n");
        } else {
            System.out.println("\nExiting as success.\n");
        }

    }

    private void execFunction(String suiteName, TestFunction testFunction) {
        int maxRuns = 1;
        if (context.getOptions().isAutoRetry()) {
            maxRuns = 2;
        }
        boolean lastRun = true;
        for (int x = 0; x < maxRuns; x++) {
            if ((x + 1) < maxRuns) {
                lastRun = false;
            }
            try {
                testFunction.getFunc().call(this, context.getDriver(), context.getHelper());
                successCount++;
                succeeded.add(suiteName + "." + testFunction.getName());
                break;
            } catch (AssertionError e) {
                String execMessage = e.getMessage();
                String msg = "AssertionError running " + suiteName + "." + testFunction.getName() + ":" + e.getMessage();
                System.err.println(msg);
                ExceptionUtils.printRootCauseStackTrace(e);
                int i = msg.indexOf("\n");
                if (i == -1) {
                    i = msg.length();
                }
                if (i > 120) {
                    i = 120;
                }
                msg = msg.substring(0, i);
                if (lastRun) {
                    errors.add(new TestError()
                            .setAssertError(true)
                            .setMsg(msg)
                            .setSuite(suiteName)
                            .setTest(testFunction.getName()));
                }
            } catch (Exception e) {
                String msg = e.toString() + " running " + suiteName + "." + testFunction.getName() + ":" + e.getMessage();
                int i = msg.indexOf("\n");
                if (i == -1) {
                    i = msg.length();
                }
                if (i > 120) {
                    i = 120;
                }
                msg = msg.substring(0, i);
                System.err.println(msg);
                ExceptionUtils.printRootCauseStackTrace(e);
                if (lastRun) {
                    errors.add(new TestError()
                            .setAssertError(true)
                            .setMsg(msg)
                            .setSuite(suiteName)
                            .setTest(testFunction.getName()));
                }

            }
        }
    }
}
