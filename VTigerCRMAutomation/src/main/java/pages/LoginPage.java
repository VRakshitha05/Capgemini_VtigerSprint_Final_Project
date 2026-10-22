package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import utilities.ConfigReader;
import utilities.WebDriverUtility;

public class LoginPage {

    WebDriver driver;
    WebDriverUtility wu = new WebDriverUtility();
    String url = ConfigReader.get("url");

    By username = By.name("user_name");
    By password = By.name("user_password");
    By loginButton = By.xpath("//input[@type='submit' and (@name='Login' or contains(@value,'Login'))]");
    By calendarMenu = By.linkText("Calendar");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void openLoginPage() {
        wu.openUrl(driver, url);
    }

    public void login(String user, String pass) {
        wu.type(driver, username, user);
        wu.type(driver, password, pass);
        wu.click(driver, loginButton);
    }

    public void logout() {
        wu.openUrl(driver, url + "index.php?module=Users&action=Logout");
    }

    public boolean isLoginPageDisplayed() {
        return wu.isVisible(driver, username, 5) && wu.isVisible(driver, password, 2);
    }

    // the Calendar menu is visible only after a successful login
    public boolean isLoggedIn() {
        return wu.isVisible(driver, calendarMenu, 20);
    }
}
