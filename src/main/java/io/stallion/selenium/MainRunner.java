package io.stallion.selenium;

import jdk.nashorn.tools.Shell;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.util.concurrent.TimeUnit;


public class MainRunner {
    public static void main(String[] args)  {
        CommandOptions options = new CommandOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("\n\nError!\n\n" + e.getMessage());
            System.err.println("\n\nAllowed options: \n");
            parser.printUsage(System.err);
            System.err.println("\n");
            System.exit(1);
        }
        WebDriver driver = null;
        try {

            if ("firefox".equals(options.getBrowser())) {

                driver = new FirefoxDriver();
            } else if ("chrome".equals(options.getBrowser())) {
                driver = new ChromeDriver();
            } else if ("safari".equals(options.getBrowser())) {
                driver = new SafariDriver();


            } else if ("explorer".equals(options.getBrowser())) {
                driver = new InternetExplorerDriver();
            } else if ("htmlunit".equals(options.getBrowser())) {
                driver = new HtmlUnitDriver(true);
            } else if (!"".equals(options.getDriverClass())) {
                Class cls = MainRunner.class.getClassLoader().loadClass(options.getDriverClass());
                driver = (WebDriver)cls.newInstance();
            } else {
                throw new RuntimeException("You did not choose a recognized -browser option: " + options.getBrowser());
            }
        } catch (IllegalAccessException|ClassNotFoundException|InstantiationException e) {
            System.err.println("Could not load browser webdriver for " + options.getDriverClass());
            throw new RuntimeException(e);
        }

        DriverHelper helper = new DriverHelper(driver);
        helper.setBaseUrl(options.getBaseUrl());
        SeleniumContext context = new SeleniumContext()
                .setHelper(helper)
                .setDriver(driver)
                .setOptions(options);

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);


        String[] argArray = new String[options.getArguments().size()];
        argArray = options.getArguments().toArray(argArray);
        int result = 1;
        try {
            result = JavascriptShell.main(argArray, context, options);
        } finally {
            driver.quit();
        }
        System.exit(result);


    }
}
