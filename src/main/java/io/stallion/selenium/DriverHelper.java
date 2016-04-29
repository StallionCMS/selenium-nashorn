package io.stallion.selenium;

import com.thoughtworks.selenium.SeleniumException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class DriverHelper {
    private WebDriver driver;
    private int sleepTime = 10;
    private int defaultTimeout = 5000;

    private List<Integer> range(int timeout) {
        List<Integer> counts = new ArrayList<>();
        int times = timeout/ sleepTime;
        for(int x=0; x<times; x++) {
            counts.add(x);
        }
        return counts;
    }

    public void waitTrue(Function<Object, Boolean> func) {
        for(int x: range(defaultTimeout)) {
            boolean result = func.apply(this);
            if (result) {
                return;
            }
            sleep();
        }
        throw new AssertionError("Function failed until timeout " + func);
    }


    public DriverHelper(WebDriver driver) {
        this.driver = driver;

    }

    public void open(String url, String selector) {
        driver.get(url);
        driver.findElement(By.cssSelector(selector));
    }

    public WebElement find(String selector) {
        return find(selector, defaultTimeout);
    }

    public WebElement find(String selector, int timeout) {
        WebElement element = null;
        for(int x: range(timeout)) {
            element = driver.findElement(By.cssSelector(selector));
            if (element != null) {
                break;
            }
            sleep();
        }
        if (element == null) {
            throw new SeleniumException("Could not find element for selector: " + selector);
        }
        return element;
    }

    private void sleep() {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public WebElement findAll(String selector, int timeout) {
        return null;
    }

    public WebElement assertExists(String selector, int timeout) {
        return null;
    }

    public WebElement assertTextExists(String selector, String text, int timeout) {
        return null;
    }




}
