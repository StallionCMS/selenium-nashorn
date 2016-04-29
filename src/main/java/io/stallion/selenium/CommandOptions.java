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
}
