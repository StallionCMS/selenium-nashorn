package io.stallion.selenium;

import org.openqa.selenium.WebDriver;



public class SeleniumContext {
    private WebDriver driver;
    private DriverHelper helper;
    private CommandOptions options;

    public WebDriver getDriver() {
        return driver;
    }

    public SeleniumContext setDriver(WebDriver driver) {
        this.driver = driver;
        return this;
    }

    public DriverHelper getHelper() {
        return helper;
    }

    public SeleniumContext setHelper(DriverHelper helper) {
        this.helper = helper;
        return this;
    }

    public CommandOptions getOptions() {
        return options;
    }

    public SeleniumContext setOptions(CommandOptions options) {
        this.options = options;
        return this;
    }
}
