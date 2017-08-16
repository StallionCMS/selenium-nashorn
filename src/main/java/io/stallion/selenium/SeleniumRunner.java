package io.stallion.selenium;

import jdk.nashorn.api.scripting.JSObject;
import jline.internal.Log;
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
                continue;
            } else if ("afterSuite".equals(name)) {
                suite.setBeforeSuite((JSObject) o.getMember(name));
                continue;
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
            try {
                if (suite.getBeforeSuite() != null) {
                    suite.getBeforeSuite().call(this, context.getDriver(), context.getHelper());
                }
            } catch (Exception e) {
                Log.error("Error running beforeSuite for " + suite.getName());
                ExceptionUtils.printRootCauseStackTrace(e);
                if (!context.getOptions().isAutoRetry()) {
                    errors.add(new TestError()
                            .setAssertError(true)
                            .setMsg(e.getMessage())
                            .setSuite(suite.getName())
                            .setTest("afterSuite"));
                    continue;
                } else {
                    try {
                        Log.info("Retry beforeSuite for " + suite.getName());
                        suite.getBeforeSuite().call(this, context.getDriver(), context.getHelper());
                    } catch (Exception e2) {
                        ExceptionUtils.printRootCauseStackTrace(e2);
                        errors.add(new TestError()
                                .setAssertError(true)
                                .setMsg(e2.getMessage())
                                .setSuite(suite.getName())
                                .setTest("beforeSuite"));
                        continue;
                    }
                }

            }

            for (TestFunction testFunction :suite.getTests()) {
                if (!"".equals(testName)) {
                    if (!testName.equals(testFunction.getName())) {
                        continue;
                    }
                }
                execTestWithRetry(testFunction, suite);
            }
            try {
                if (suite.getAfterSuite() != null) {
                    suite.getAfterSuite().call(this, context.getDriver(), context.getHelper());
                }
            } catch (Exception e) {
                Log.error("Error running afterSuite for " + suite.getName());
                ExceptionUtils.printRootCauseStackTrace(e);
                if (!context.getOptions().isAutoRetry()) {
                    errors.add(new TestError()
                            .setAssertError(true)
                            .setMsg(e.getMessage())
                            .setSuite(suite.getName())
                            .setTest("afterSuite"));
                    continue;
                } else {
                    try {
                        Log.info("Retrying afterSuite for " + suite.getName());
                        suite.getAfterSuite().call(this, context.getDriver(), context.getHelper());
                    } catch (Exception e2) {
                        ExceptionUtils.printRootCauseStackTrace(e2);
                        errors.add(new TestError()
                                .setAssertError(true)
                                .setMsg(e2.getMessage())
                                .setSuite(suite.getName())
                                .setTest("afterSuite"));
                        continue;
                    }
                }
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

    private void execTestWithRetry(TestFunction testFunction, Suite suite) {
        int maxRuns = 1;
        if (context.getOptions().isAutoRetry()) {
            maxRuns = 2;
        }
        boolean isLastRun = true;
        for (int x = 0; x < maxRuns; x++) {
            if ((x + 1) < maxRuns) {
                isLastRun = false;
            } else {
                isLastRun = true;
            }
            if (x > 0) {
                System.err.println("Test failed first time, retrying: " + suiteName + ":" + testFunction.getName());
            }
            try {
                if (suite.getBefore() != null) {
                    suite.getBefore().call(this, context.getDriver(), context.getHelper());
                }

                testFunction.getFunc().call(this, context.getDriver(), context.getHelper());

                if (suite.getAfter() != null) {
                    suite.getAfter().call(this, context.getDriver(), context.getHelper());
                }
                successCount++;
                succeeded.add(suite.getName() + "." + testFunction.getName());
                break;

            } catch (AssertionError e) {
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
                if (isLastRun) {
                    errors.add(new TestError()
                            .setAssertError(true)
                            .setMsg(msg)
                            .setSuite(suite.getName())
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
                if (isLastRun) {
                    errors.add(new TestError()
                            .setAssertError(false)
                            .setMsg(msg)
                            .setSuite(suite.getName())
                            .setTest(testFunction.getName()));
                }

            }
        }

    }

}
