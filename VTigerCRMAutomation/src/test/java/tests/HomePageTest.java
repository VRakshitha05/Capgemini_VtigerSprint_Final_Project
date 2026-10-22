package tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import base.BaseClass;

public class HomePageTest extends BaseClass {

    @Test(priority = 1, groups = {"Smoke", "Positive"}, description = "TC06 Home page loads after login")
    public void homePageLoads_Test() throws Exception {

        hp.openHome();
        Thread.sleep(3000);

        Assert.assertTrue(driver.getCurrentUrl().contains("module=Home"), "Not on Home page");
        Assert.assertTrue(hp.isMenuDisplayed("Calendar"), "Menu is not displayed");

        Reporter.log("Home page opened and menu is displayed", true);
    }
}