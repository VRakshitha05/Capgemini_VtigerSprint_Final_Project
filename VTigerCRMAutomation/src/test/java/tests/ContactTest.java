package tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import base.BaseClass;
import utilities.ExcelUtility;
import utilities.WebDriverUtility;

public class ContactTest extends BaseClass {

    ExcelUtility eu = new ExcelUtility();
    WebDriverUtility wu = new WebDriverUtility();

    // creates a contact with last name and email and returns the last name
    public String createContact(String baseName, String email) throws Exception {

        String lastName = baseName + "_" + System.currentTimeMillis() % 1000000;

        cp.openCreateForm();
        cp.fillForm(lastName, "", "", email);
        Thread.sleep(3000);
        cp.clickSave();

        Assert.assertTrue(cp.isDetailPage(lastName), "Contact was not saved: " + lastName);
        Reporter.log("Contact created: " + lastName, true);
        return lastName;
    }

    // deletes the contact from the list (used to clean the test data)
    public void deleteContact(String lastName) {

        cp.openList();
        cp.search(lastName);
        if (cp.isInList(lastName)) {
            cp.deleteInRow(lastName);
            Reporter.log("Contact deleted: " + lastName, true);
        }
    }

    // creates an organization and returns its name
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

    public void deleteOrg(String name) {

        op.openList();
        op.search(name);
        if (op.isInList(name)) {
            op.deleteInRow(name);
            Reporter.log("Organization deleted: " + name, true);
        }
    }

    @Test(priority = 1, groups = {"Smoke", "Positive"}, description = "TC26 Contacts list page loads")
    public void listLoads_Test() throws Exception {

        cp.openList();
        Thread.sleep(3000);

        Assert.assertTrue(cp.isListLoaded(), "Contacts list did not load");

        Reporter.log("Contacts list opened", true);
    }

    @Test(priority = 2, groups = {"Negative"}, description = "TC27 Create contact with blank last name")
    public void blankLastName_Test() throws Exception {

        cp.openCreateForm();
        cp.fillForm("", "", "", "");
        Thread.sleep(3000);
        cp.clickSave();

        String alert = wu.getAlertText(driver, 5);
        Reporter.log("Alert shown: " + alert, true);
        takeScreenshot("TC27_BlankLastName");

        Assert.assertNotNull(alert, "Alert for blank last name did not come");
        Assert.assertTrue(cp.isOnEditForm(), "User should stay on the create form");

        Reporter.log("Contact not created because the last name is blank", true);
    }

    // the contact is created for the organization that we create first
    @Test(priority = 3, groups = {"Integration", "Positive"}, description = "TC28 Create contact for an organization")
    public void createContactForOrganization_Test() throws Exception {

        String orgName = createOrg("QA_Org_Contact");
        String lastName = eu.getDataFromExcel("Contacts", 1, 1) + "_" + System.currentTimeMillis() % 1000000;
        String title = eu.getDataFromExcel("Contacts", 1, 3);
        String email = eu.getDataFromExcel("Contacts", 1, 4);

        cp.openCreateForm();
        cp.fillForm(lastName, "", title, email);
        cp.selectOrganization(orgName);
        Reporter.log("Organization selected for the contact: " + orgName, true);
        Thread.sleep(3000);
        cp.clickSave();

        Assert.assertTrue(cp.isDetailPage(lastName), "Contact was not saved: " + lastName);
        Assert.assertTrue(driver.getPageSource().contains(orgName), "Organization name is not shown on the contact page");
        Reporter.log("Contact created for the organization: " + lastName + " -> " + orgName, true);
        Thread.sleep(3000);

        // the contact is shown in the list with the organization name
        cp.openList();
        cp.search(lastName);
        Assert.assertTrue(cp.isInList(lastName), "Contact not found in the list");
        Assert.assertTrue(cp.isTextInRow(lastName, orgName), "Organization name is not shown in the contact row");
        Reporter.log("Contact found in the list with its organization name", true);
        Thread.sleep(3000);

        // clean the test data: contact first, then the organization
        cp.deleteInRow(lastName);
        Reporter.log("Contact deleted: " + lastName, true);
        cp.openList();
        cp.search(lastName);
        boolean gone = cp.isGone(lastName);
        deleteOrg(orgName);

        Assert.assertTrue(gone, "Contact is still in the list after delete");
    }

    @Test(priority = 4, groups = {"Functional", "Positive"}, description = "TC29 Mass edit the title of a contact")
    public void massEditContact_Test() throws Exception {

        String lastName = createContact(eu.getDataFromExcel("Contacts", 3, 1), eu.getDataFromExcel("Contacts", 3, 4));
        String newTitle = eu.getDataFromExcel("Contacts", 3, 5);

        cp.openList();
        cp.search(lastName);
        Assert.assertTrue(cp.isInList(lastName), "Contact not found in the list");
        cp.tickRow(lastName);
        cp.clickMassEdit();
        Reporter.log("Mass Edit popup opened", true);
        Thread.sleep(3000);

        cp.massEditTitle(newTitle);
        Thread.sleep(3000);

        cp.openList();
        cp.search(lastName);
        boolean changed = cp.isTextInRow(lastName, newTitle);
        Reporter.log("Title changed by mass edit to: " + newTitle, true);

        deleteContact(lastName);
        Assert.assertTrue(changed, "Title was not updated by mass edit");
    }

    @Test(priority = 5, groups = {"Functional", "Positive"}, description = "TC30 Send mail to a contact")
    public void sendMailToContact_Test() throws Exception {

        String lastName = createContact(eu.getDataFromExcel("Contacts", 4, 1), eu.getDataFromExcel("Contacts", 4, 4));
        String subject = eu.getDataFromExcel("Contacts", 4, 6);

        cp.openList();
        cp.search(lastName);
        Assert.assertTrue(cp.isInList(lastName), "Contact not found in the list");
        cp.tickRow(lastName);
        cp.clickSendMail();
        Thread.sleep(3000);
        Assert.assertTrue(cp.isSelectEmailPopupDisplayed(), "Select Email IDs popup did not open");
        Reporter.log("Select Email IDs popup opened", true);

        String mainWindow = driver.getWindowHandle();
        cp.clickSelectEmail();
        wu.switchToNewWindow(driver, mainWindow);          // the Compose E-Mail window
        Assert.assertTrue(cp.isComposeWindowOpen(), "Compose E-Mail window did not open");
        Reporter.log("Compose E-Mail window opened", true);

        cp.enterSubject(subject);
        Thread.sleep(3000);
        Assert.assertEquals(cp.getSubject(), subject, "Subject was not typed");
        Reporter.log("Mail subject typed: " + subject, true);

        driver.close();                                     // close the compose window
        driver.switchTo().window(mainWindow);

        deleteContact(lastName);
    }
}