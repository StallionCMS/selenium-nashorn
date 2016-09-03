package io.stallion.selenium;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;


public class CommandOptions {
    @Option(name="-browser", usage="The browser to use.")
    private String browser = "firefox";

    @Option(name="-driverClass", usage="The class name of the WebDriver to use.")
    private String driverClass = "";

    @Option(name="-tests", usage="Limit test running to a subset of the tests: -tests=suiteName or -tests=suiteName:testName")
    private String tests = "";

    @Option(name="-baseUrl", usage="The base url for the web pages you are testing. Defaults to http://localhost:8090")
    private String baseUrl = "http://localhost:8090";

    @Option(name="-remoteConfFile", usage = "A JSON file with settings for a remote server")
    private String remoteConfFile = "";

    @Option(name="-remoteUrl", usage = "The URL of the remote server")
    private String remoteUrl = "";

    @Option(name="-autoRetry", usage = "Automatically retry a test once if it fails the first time.")
    private boolean autoRetry = false;


    @Argument
    private List<String> arguments = new ArrayList<String>();

    public String getBrowser() {
        return browser;
    }

    public CommandOptions setBrowser(String browser) {
        this.browser = browser;
        return this;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public CommandOptions setDriverClass(String driverClass) {
        this.driverClass = driverClass;
        return this;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public CommandOptions setArguments(List<String> arguments) {
        this.arguments = arguments;
        return this;
    }

    public String getTests() {
        return tests;
    }

    public CommandOptions setTests(String tests) {
        this.tests = tests;
        return this;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public CommandOptions setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public String getRemoteConfFile() {
        return remoteConfFile;
    }

    public CommandOptions setRemoteConfFile(String remoteConfFile) {
        this.remoteConfFile = remoteConfFile;
        return this;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public CommandOptions setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
        return this;
    }


    public boolean isAutoRetry() {
        return autoRetry;
    }

    public CommandOptions setAutoRetry(boolean autoRetry) {
        this.autoRetry = autoRetry;
        return this;
    }
}
