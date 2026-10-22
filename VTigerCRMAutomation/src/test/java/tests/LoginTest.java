
package tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import base.BaseClass;
import utilities.ConfigReader;
import utilities.ExcelUtility;

public class LoginTest extends BaseClass {

    ExcelUtility eu = new ExcelUtility();

    private void ensureLoginPage() {

        if (lp.isLoginPageDisplayed()) {
            return;
        }

        lp.logout();

        if (waitForLoginPage(10)) {
            return;
        }

        driver.manage().deleteAllCookies();
        lp.openLoginPage();

        Assert.assertTrue(
            waitForLoginPage(15),
            "Login page could not be opened"
        );
    }

    @Test(
        priority = 1,
        groups = {"Smoke", "Positive"},
        description = "TC01 Login with valid credentials"
    )
    public void validLogin_Test() {

        ensureLoginPage();

        lp.login(
            ConfigReader.get("username"),
            ConfigReader.get("password")
        );

        Assert.assertTrue(
            lp.waitForLoggedIn(20),
            "Home page was not displayed after valid login"
        );

        Reporter.log("TC01: Valid login successful", true);
    }

    @Test(
        priority = 2,
        groups = {"Negative"},
        description = "TC02 Login with invalid credentials"
    )
    public void invalidLogin_Test() throws Exception {

        ensureLoginPage();

        String user = eu.getDataFromExcel("Login", 1, 1);
        String pass = eu.getDataFromExcel("Login", 1, 2);

        lp.login(user, pass);


Assert.assertTrue(
    waitForLoginPage(10),
    "Login form should remain visible after blank-password submission"
);


        takeScreenshot("TC02_InvalidLogin");

        Reporter.log("TC02: Invalid login rejected", true);
    }

    @Test(
        priority = 3,
        groups = {"Negative"},
        description = "TC03 Login with blank username"
    )
    public void blankUsername_Test() throws Exception {

        ensureLoginPage();

        String user = eu.getDataFromExcel("Login", 2, 1);
        String pass = eu.getDataFromExcel("Login", 2, 2);

        lp.login(user, pass);

        Assert.assertTrue(
            lp.isLoginPageDisplayed(),
            "Login page should remain displayed after blank username"
        );

        takeScreenshot("TC03_BlankUsername");

        Reporter.log("TC03: Blank username rejected", true);
    }

    @Test(
        priority = 4,
        groups = {"Negative"},
        description = "TC04 Login with blank password"
    )
    public void blankPassword_Test() throws Exception {

        ensureLoginPage();

        String user = eu.getDataFromExcel("Login", 3, 1);
        String pass = eu.getDataFromExcel("Login", 3, 2);

        lp.login(user, pass);

        Assert.assertTrue(
        	    waitForLoginPage(10),
        	    "Login page should remain displayed after blank password"
        	);

        takeScreenshot("TC04_BlankPassword");

        Reporter.log("TC04: Blank password rejected", true);
    }


@Test(
    priority = 5,
    groups = {"System", "Positive"},
    description = "TC05 Home page inaccessible after logout"
)
public void logoutThenOpenHome_Test() {

    // First, make sure the login page is available
    ensureLoginPage();

    // Login with valid credentials
    lp.login(
        ConfigReader.get("username"),
        ConfigReader.get("password")
    );

    Assert.assertTrue(
        lp.waitForLoggedIn(20),
        "Login failed before logout test"
    );

    // Logout from VTiger
    lp.logout();

    // Wait for the login page after logout
    boolean loginPageDisplayed = waitForLoginPage(15);

    // If it is not detected, reopen the login URL and check again
    if (!loginPageDisplayed) {
        lp.openLoginPage();
        loginPageDisplayed = waitForLoginPage(15);
    }

    Assert.assertTrue(
        loginPageDisplayed,
        "Login page was not displayed after logout"
    );

    // Try to access Home after logout
    driver.get(
        ConfigReader.get("url")
            + "index.php?module=Home&action=index"
    );

    Assert.assertTrue(
        waitForLoginPage(15),
        "Login page should appear when accessing Home after logout"
    );

    Reporter.log(
        "TC05: Home page inaccessible after logout",
        true
    );
}
}