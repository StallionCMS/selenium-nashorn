package io.stallion.selenium;

public class TestError {
    private boolean assertError;
    private String stackTrace;
    private String msg;
    private String suite;
    private String test;


    public boolean isAssertError() {
        return assertError;
    }

    public TestError setAssertError(boolean assertError) {
        this.assertError = assertError;
        return this;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public TestError setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public TestError setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getSuite() {
        return suite;
    }

    public TestError setSuite(String suite) {
        this.suite = suite;
        return this;
    }

    public String getTest() {
        return test;
    }

    public TestError setTest(String test) {
        this.test = test;
        return this;
    }
}
