package tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import base.BaseClass;
import utilities.ExcelUtility;
import utilities.WebDriverUtility;

public class OrganizationTest extends BaseClass {

    ExcelUtility eu = new ExcelUtility();
    WebDriverUtility wu = new WebDriverUtility();

    // creates an organization with only the name and returns the name
    public String createOrg(String baseName) throws Exception {

        String name = baseName + "_" + System.currentTimeMillis() % 1000000;

        op.openCreateForm();
        op.fillForm(name, "", "", "", "");
        Thread.sleep(3000);
        op.clickSave();

        Assert.assertTrue(op.isDetailPage(name), "Organization was not saved: " + name);
        Reporter.log("Organization created: " + name, true);
        return name;
    }

    // deletes the organization from the list (used to clean the test data)
    public void deleteOrg(String name) {

        op.openList();
        op.search(name);
        if (op.isInList(name)) {
            op.deleteInRow(name);
            Reporter.log("Organization deleted: " + name, true);
        }
    }

    @Test(priority = 1, groups = {"Smoke", "Positive"}, description = "TC20 Organizations list page loads")
    public void listLoads_Test() throws Exception {

        op.openList();
        Thread.sleep(3000);

        Assert.assertTrue(op.isListLoaded(), "Organizations list did not load");

        Reporter.log("Organizations list opened", true);
    }

    @Test(priority = 2, groups = {"Functional", "Positive"}, description = "TC21 Create organization with valid data")
    public void createValidOrg_Test() throws Exception {

        String name = eu.getDataFromExcel("Organization", 1, 1) + "_" + System.currentTimeMillis() % 1000000;
        String website = eu.getDataFromExcel("Organization", 1, 2);
        String phone = eu.getDataFromExcel("Organization", 1, 3);
        String email = eu.getDataFromExcel("Organization", 1, 4);
        String employees = eu.getDataFromExcel("Organization", 1, 5);

        op.openCreateForm();
        op.fillForm(name, website, phone, email, employees);
        Thread.sleep(3000);
        op.clickSave();

        boolean saved = op.isDetailPage(name);
        Reporter.log("Organization created with all details: " + name, true);
        Thread.sleep(3000);
        deleteOrg(name);

        Assert.assertTrue(saved, "Organization was not saved: " + name);
    }

    @Test(priority = 3, groups = {"Negative"}, description = "TC22 Create organization with blank name")
    public void blankName_Test() throws Exception {

        op.openCreateForm();
        op.fillForm("", "", "", "", "");
        Thread.sleep(3000);
        op.clickSave();

        String alert = wu.getAlertText(driver, 5);
        Reporter.log("Alert shown: " + alert, true);
        takeScreenshot("TC22_BlankOrgName");

        Assert.assertNotNull(alert, "Alert for blank name did not come");
        Assert.assertTrue(op.isOnEditForm(), "User should stay on the create form");

        Reporter.log("Organization not created because the name is blank", true);
    }

    @Test(priority = 4, groups = {"Negative"}, description = "TC23 Create organization with invalid email")
    public void invalidEmail_Test() throws Exception {

        String name = eu.getDataFromExcel("Organization", 3, 1) + "_" + System.currentTimeMillis() % 1000000;
        String email = eu.getDataFromExcel("Organization", 3, 4);

        op.openCreateForm();
        op.fillForm(name, "", "", email, "");
        Thread.sleep(3000);
        op.clickSave();

        String alert = wu.getAlertText(driver, 5);
        Reporter.log("Alert shown: " + alert, true);
        takeScreenshot("TC23_InvalidOrgEmail");

        if (alert == null) {
            deleteOrg(name);   // the application saved a wrong record, so remove it
        }
        Assert.assertNotNull(alert, "Alert for invalid email did not come");
        Assert.assertTrue(op.isOnEditForm(), "User should stay on the create form");

        Reporter.log("Organization not created because the email is invalid", true);
    }

    // create, search, edit (shown once) and then delete
    @Test(priority = 5, groups = {"System", "Positive"}, description = "TC24 Create, search, edit and delete an organization")
    public void organizationLifecycle_Test() throws Exception {

        String name = createOrg(eu.getDataFromExcel("Organization", 5, 1));
        String newName = name + "_Edited";
        Thread.sleep(3000);

        // search
        op.openList();
        op.search(name);
        Assert.assertTrue(op.isInList(name), "Created organization not found");
        Reporter.log("Organization found in the list: " + name, true);
        Thread.sleep(3000);

        // edit
        op.clickEditInRow(name);
        op.changeName(newName);
        Thread.sleep(3000);
        op.clickSave();
        op.waitEditSaved();
        Reporter.log("Organization edited: " + name + " changed to " + newName, true);
        Thread.sleep(3000);

        // search the new name
        op.openList();
        op.search(newName);
        Assert.assertTrue(op.isInList(newName), "Edited organization not found");
        Reporter.log("Edited organization found in the list: " + newName, true);
        Thread.sleep(3000);

        // delete
        op.deleteInRow(newName);
        Reporter.log("Organization deleted: " + newName, true);
        Thread.sleep(3000);

        // search again, it must be gone
        op.openList();
        op.search(newName);
        Assert.assertTrue(op.isGone(newName), "Organization is still in the list after delete");

        Reporter.log("Organization is no longer in the list", true);
    }

    @Test(priority = 6, groups = {"Negative"}, description = "TC25 Click Delete without selecting a record")
    public void deleteWithoutSelection_Test() throws Exception {

        op.openList();
        op.clickDeleteButton();

        String alert = wu.getAlertText(driver, 5);
        Reporter.log("Alert shown: " + alert, true);
        takeScreenshot("TC25_DeleteWithoutSelection");
        Thread.sleep(3000);

        Assert.assertNotNull(alert, "Alert did not come");
    }
}