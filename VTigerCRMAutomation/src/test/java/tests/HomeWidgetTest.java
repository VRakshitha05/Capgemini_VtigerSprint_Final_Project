package tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import base.BaseClass;
import utilities.ExcelUtility;
import utilities.WebDriverUtility;

// These tests ADD widgets to the Home page. The Home page is shared by everybody and the
// widgets are not removed again, so these tests are NOT in AllTestCases.xml.
// Run them only when needed with HomeWidgets.xml and delete the QA_ widgets by hand afterwards.
public class HomeWidgetTest extends BaseClass {

    ExcelUtility eu = new ExcelUtility();
    WebDriverUtility wu = new WebDriverUtility();

    @Test(priority = 1, groups = {"Functional", "Positive"}, description = "TC07 Add Module widget with valid title")
    public void addModuleWidget_Test() throws Exception {

        String title = eu.getDataFromExcel("Home", 1, 1) + "_" + System.currentTimeMillis() % 100000;

        hp.openHome();
        hp.openModulePopup();
        hp.enterTitle(title);
        Thread.sleep(3000);
        hp.selectFirstTwoFields();
        Thread.sleep(3000);
        hp.clickSave();

        Assert.assertTrue(hp.isPopupClosed(), "Add Module popup did not close");
        Thread.sleep(3000);
        Assert.assertTrue(hp.isTitleOnHome(title), "Widget is not shown on Home page");

        Reporter.log("Module widget added: " + title, true);
    }

    @Test(priority = 2, groups = {"Functional", "Positive"}, description = "TC08 Add Dashboard widget with valid title")
    public void addDashboardWidget_Test() throws Exception {

        String title = eu.getDataFromExcel("Home", 2, 1) + "_" + System.currentTimeMillis() % 100000;

        hp.openHome();
        hp.openDashboardPopup();
        hp.enterTitle(title);
        Thread.sleep(3000);
        hp.selectDashboardByIndex(0, 1);
        Thread.sleep(3000);
        hp.clickSave();

        Assert.assertTrue(hp.isPopupClosed(), "Add Dashboard popup did not close");
        Thread.sleep(3000);
        Assert.assertTrue(hp.isTitleOnHome(title), "Widget is not shown on Home page");

        Reporter.log("Dashboard widget added: " + title, true);
    }

    @Test(priority = 3, groups = {"Functional", "Positive"}, description = "TC09 Add Notebook widget with valid title")
    public void addNotebookWidget_Test() throws Exception {

        String title = eu.getDataFromExcel("Home", 3, 1) + "_" + System.currentTimeMillis() % 100000;

        hp.openHome();
        hp.openNotebookPopup();
        hp.enterTitle(title);
        Thread.sleep(3000);
        hp.clickSave();

        Assert.assertTrue(hp.isPopupClosed(), "Add Notebook popup did not close");
        Thread.sleep(3000);
        Assert.assertTrue(hp.isTitleOnHome(title), "Widget is not shown on Home page");

        Reporter.log("Notebook widget added: " + title, true);
    }

    @Test(priority = 4, groups = {"Negative"}, description = "TC10 Add Dashboard widget with blank Window Title")
    public void dashboardWithoutTitle_Test() throws Exception {

        hp.openHome();
        hp.openDashboardPopup();
        hp.enterTitle("");
        hp.selectDashboardByIndex(0, 1);
        Thread.sleep(3000);
        hp.clickSave();

        String alert = wu.getAlertText(driver, 5);
        Reporter.log("Alert shown: " + alert, true);
        takeScreenshot("TC10_DashboardBlankTitle");

        Assert.assertEquals(alert, "Please enter Window Title.", "Wrong or missing alert message");
    }
}