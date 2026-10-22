
package pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utilities.ConfigReader;

public class LoginPage {

    private WebDriver driver;
    private WebDriverWait wait;
    private String url = ConfigReader.get("url");

    private By username = By.name("user_name");
    private By password = By.name("user_password");

    private By loginButton = By.xpath(
        "//input[@name='Login' or " +
        "(@type='submit' and contains(@value,'Login'))]"
    );

    private By calendarMenu = By.linkText("Calendar");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void openLoginPage() {
        driver.get(url);
    }

    public void login(String user, String pass) {
        WebElement userField = wait.until(
            ExpectedConditions.visibilityOfElementLocated(username)
        );

        userField.clear();
        userField.sendKeys(user == null ? "" : user);

        WebElement passwordField = wait.until(
            ExpectedConditions.visibilityOfElementLocated(password)
        );

        passwordField.clear();
        passwordField.sendKeys(pass == null ? "" : pass);

        wait.until(
            ExpectedConditions.elementToBeClickable(loginButton)
        ).click();
    }


public void logout() {
    driver.get(url + "index.php?module=Users&action=Logout");

    try {
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(d -> isLoginPageDisplayed());
    } catch (Exception e) {
        // If logout does not show the login form, open the login URL.
        driver.manage().deleteAllCookies();
        driver.get(url);

        new WebDriverWait(driver, Duration.ofSeconds(15))
            .until(d -> isLoginPageDisplayed());
    }

    System.out.println("Login page displayed after logout.");
}



public boolean isLoginPageDisplayed() {
    try {
        return !driver.findElements(username).isEmpty()
            && !driver.findElements(password).isEmpty()
            && !driver.findElements(loginButton).isEmpty()
            && driver.findElement(username).isDisplayed()
            && driver.findElement(password).isDisplayed()
            && driver.findElement(loginButton).isDisplayed();
    } catch (Exception e) {
        return false;
    }
}


    public boolean isLoggedIn() {
        try {
            return driver.findElements(calendarMenu)
                .stream()
                .anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean waitForLoggedIn(int seconds) {
        long endTime = System.currentTimeMillis() + seconds * 1000L;

        while (System.currentTimeMillis() < endTime) {
            if (isLoggedIn()) {
                return true;
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }
}
