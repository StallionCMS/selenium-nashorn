package io.stallion.selenium;

import com.thoughtworks.selenium.SeleniumException;
import org.openqa.selenium.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


public class DriverHelper {
    private WebDriver driver;
    private int sleepTime = 10;
    private int defaultTimeout = 5000;
    private String baseUrl = "";

    public void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public void assertHasText(String selector, String text) {
        assertHasText(driver.findElement(By.cssSelector(selector)), text);
    }

    public void assertNotHasText(String selector, String text) {
        assertNotHasText(driver.findElement(By.cssSelector(selector)), text);
    }

    public void assertHasHtml(String selector, String text) {
        assertHasHtml(driver.findElement(By.cssSelector(selector)), text);
    }

    public void assertHasText(WebElement ele, String text) {
        String actual = ele.getText();
        assertTrue(actual.indexOf(text) > -1, "Text not found. Text actual: \n" + actual + "\n\nText expected:\n" + text);
    }

    public void assertNotHasText(WebElement ele, String text) {
        String actual = ele.getText();
        assertTrue(actual.indexOf(text) == -1, "Text found when not supposed to exist. Text actual: \n" + actual + "\n\nUnexpected text:\n" + text);
    }


    public void assertHasHtml(WebElement ele, String text) {
        String actual = ele.getAttribute("innerHTML");
        assertTrue(actual.indexOf(text) > -1, "HTML not found. HTML actual: \n" + actual + "\n\nHTML expected:\n" + text);
    }


    private List<Integer> range(int timeout) {
        List<Integer> counts = new ArrayList<>();
        int times = timeout/ sleepTime;
        for(int x=0; x<times; x++) {
            counts.add(x);
        }
        return counts;
    }

    public boolean exists(String selector) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.MILLISECONDS);
        try {
            List<WebElement> eles = driver.findElements(By.cssSelector(selector));
            if (eles.size() > 0) {
                return true;
            } else {
                return false;
            }
        } finally {
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        }
    }

    public void waitNotExists(String selector) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.MILLISECONDS);
        try {
            for (int x : range(defaultTimeout)) {
                List<WebElement> eles = driver.findElements(By.cssSelector(selector));
                if (eles.size() == 0) {
                    return;
                }
                sleep();
            }
        } finally {
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        }
        throw new AssertionError("Selector still exists:" + selector);
    }

    public void waitVisible(String selector) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.MILLISECONDS);
        try {

            for (int x : range(defaultTimeout)) {
                List<WebElement> eles = driver.findElements(By.cssSelector(selector));
                if (eles.size() > 0) {
                    if (eles.get(0).isDisplayed()) {
                        return;
                    }

                }
                sleep();
            }
            throw new AssertionError("Selector not displayed:" + selector);
        } finally {
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        }
    }

    public WebElement getParent(WebElement ele) {
        return ele.findElement(By.xpath(".."));
    }

    public WebElement getParentBySelector(WebElement ele, String selector) {
        return (WebElement)((JavascriptExecutor)driver).executeScript("return arguments[0].closest(arguments[1])", ele, selector);
    }

    public void waitNotVisible(String selector) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.MILLISECONDS);
        try {

            for (int x : range(defaultTimeout)) {
                List<WebElement> eles = driver.findElements(By.cssSelector(selector));
                if (eles.size() == 0) {
                    return;
                } else {
                    try {
                        if (!eles.get(0).isDisplayed()) {
                            return;
                        }
                    } catch (StaleElementReferenceException ex) {
                        return;
                    }
                }
                sleep();
            }
            throw new AssertionError("Selector still displayed:" + selector);
        } finally {
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        }
    }


    public void waitExists(String selector) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.MILLISECONDS);
        try {

            for (int x : range(defaultTimeout)) {
                List<WebElement> eles = driver.findElements(By.cssSelector(selector));
                if (eles.size() > 0) {
                    return;
                }
                sleep();
            }
            throw new AssertionError("Selector does not exist:" + selector);
        } finally {
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        }
    }

    public void waitTextExists(String selector, String text) {
        for(int x: range(defaultTimeout)) {
            if (driver.findElement(By.cssSelector(selector)).getText().contains(text)) {
                return;
            };
            sleep();
        }
        throw new AssertionError("Text was not found with selector:" + selector + " text:" + text);
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



    public String getBaseUrl() {
        return baseUrl;
    }

    public DriverHelper setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }
}
