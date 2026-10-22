package utilities;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

// small helper methods used by all page classes
public class WebDriverUtility {

    public void waitFor(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // opens a url; if the server says "Too many connections" it waits and tries again
    public void openUrl(WebDriver driver, String url) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                driver.get(url);
            } catch (UnhandledAlertException e) {
                getAlertText(driver, 2);
                driver.get(url);
            }
            if (!driver.getPageSource().contains("Too many connections")) {
                return;
            }
            System.out.println("Server is busy, trying again " + attempt + " of 3");
            waitFor(10000);
        }
    }

    // waits until the element is visible and returns it
    // (the vtiger pages have hidden copies of some elements, so we look for the visible one)
    public WebElement getVisible(WebDriver driver, By by, int seconds) {
        for (int i = 0; i < seconds * 2; i++) {
            for (WebElement e : driver.findElements(by)) {
                try {
                    if (e.isDisplayed()) {
                        return e;
                    }
                } catch (StaleElementReferenceException ex) {
                    // the page changed, try again
                }
            }
            waitFor(500);
        }
        throw new TimeoutException("Element not visible: " + by);
    }

    public WebElement getVisible(WebDriver driver, By by) {
        return getVisible(driver, by, 15);
    }

    public boolean isVisible(WebDriver driver, By by, int seconds) {
        try {
            getVisible(driver, by, seconds);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // waits until the element is no longer visible
    public boolean isGone(WebDriver driver, By by, int seconds) {
        for (int i = 0; i < seconds * 2; i++) {
            boolean found = false;
            for (WebElement e : driver.findElements(by)) {
                try {
                    if (e.isDisplayed()) {
                        found = true;
                    }
                } catch (StaleElementReferenceException ex) {
                    // the page changed, try again
                }
            }
            if (!found) {
                return true;
            }
            waitFor(500);
        }
        return false;
    }

    public void click(WebDriver driver, By by) {
        WebElement element = getVisible(driver, by);
        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    // clears the box and types the text
    public void type(WebDriver driver, By by, String text) {
        WebElement box = getVisible(driver, by);
        box.clear();
        box.sendKeys(text);
    }

    // returns the alert text and presses OK, returns null when no alert comes
    public String getAlertText(WebDriver driver, int seconds) {
        for (int i = 0; i < seconds * 2; i++) {
            try {
                Alert alert = driver.switchTo().alert();
                String text = alert.getText();
                alert.accept();
                return text;
            } catch (NoAlertPresentException e) {
                waitFor(500);
            }
        }
        return null;
    }

    // switches to the window that was opened after the main window (popup / compose window)
    public void switchToNewWindow(WebDriver driver, String mainWindow) {
        for (int i = 0; i < 20; i++) {
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(mainWindow)) {
                    driver.switchTo().window(handle);
                    return;
                }
            }
            waitFor(500);
        }
        throw new TimeoutException("New window did not open");
    }
}
