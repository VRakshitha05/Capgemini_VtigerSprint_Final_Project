package tests;

import java.time.LocalDate;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import base.BaseClass;
import utilities.ConfigReader;
import utilities.ExcelUtility;
import utilities.WebDriverUtility;

public class CalendarTest extends BaseClass {

    ExcelUtility eu = new ExcelUtility();
    WebDriverUtility wu = new WebDriverUtility();

    @Test(priority = 1, groups = {"Smoke", "Positive"}, description = "TC11 Calendar page loads")
    public void calendarLoads_Test() throws Exception {

        calp.openCalendar();
        Thread.sleep(3000);

        Assert.assertTrue(driver.getCurrentUrl().contains("module=Calendar"), "Not on Calendar page");
        Assert.assertTrue(calp.isCalendarLoaded(), "Calendar did not load");

        Reporter.log("Calendar page opened", true);
    }

    @Test(priority = 2, groups = {"Functional", "Positive"}, description = "TC12 Add button shows Call, Meeting and To Do")
    public void addMenuOptions_Test() throws Exception {

        calp.openCalendar();
        calp.hoverAddButton();
        Thread.sleep(3000);

        Assert.assertTrue(calp.isAddOptionDisplayed("Call"), "Call is missing");
        Assert.assertTrue(calp.isAddOptionDisplayed("Meeting"), "Meeting is missing");
        Assert.assertTrue(calp.isAddOptionDisplayed("To Do"), "To Do is missing");

        Reporter.log("Add menu shows Call, Meeting and To Do", true);
    }

    @Test(priority = 3, groups = {"Functional", "Positive"}, description = "TC13 Create a Meeting event with valid data")
    public void createMeeting_Test() throws Exception {

        String name = eu.getDataFromExcel("Calendar", 1, 1) + "_" + System.currentTimeMillis() % 100000;
        LocalDate nextMonth = LocalDate.now().plusMonths(1);   // a planned event cannot be in the past

        calp.openCalendar();
        calp.openAddPopup("Meeting");
        Thread.sleep(3000);

        calp.enterEventName(name);
        calp.setDates(nextMonth, nextMonth);
        Thread.sleep(3000);
        calp.clickSave();

        String alert = wu.getAlertText(driver, 2);
        Assert.assertNull(alert, "Unexpected alert: " + alert);
        Assert.assertTrue(calp.isPopupClosed(), "Popup did not close after Save");
        Thread.sleep(3000);

        Reporter.log("Meeting created: " + name + " on " + nextMonth, true);
    }

    @Test(priority = 4, groups = {"Negative"}, description = "TC14 Save event with blank Event Name")
    public void blankEventName_Test() throws Exception {

        calp.openCalendar();
        calp.openAddPopup("Meeting");
        calp.enterEventName("");
        Thread.sleep(3000);
        calp.clickSave();

        String alert = wu.getAlertText(driver, 5);
        Reporter.log("Alert shown: " + alert, true);
        takeScreenshot("TC14_BlankEventName");

        Assert.assertNotNull(alert, "Alert for blank Event Name did not come");
        calp.clickCancel();
    }

    @Test(priority = 5, groups = {"Negative"}, description = "TC15 Planned event with a past start date")
    public void pastStartDate_Test() throws Exception {

        LocalDate yesterday = LocalDate.now().minusDays(1);

        calp.openCalendar();
        calp.openAddPopup("Meeting");
        calp.enterEventName("QA_PastDate");
        calp.setDates(yesterday, yesterday);
        Thread.sleep(3000);
        calp.clickSave();

        String alert = wu.getAlertText(driver, 5);
        Reporter.log("Alert shown: " + alert, true);
        takeScreenshot("TC15_PastStartDate");

        Assert.assertNotNull(alert, "Alert for past date did not come");
        Assert.assertTrue(alert.contains("Start Date"), "Wrong alert: " + alert);
        calp.clickCancel();
    }

    @Test(priority = 6, groups = {"Functional", "Positive"}, description = "TC16 Assigned To is the logged in user by default")
    public void assignedToDefault_Test() throws Exception {

        calp.openCalendar();
        calp.openAddPopup("Meeting");
        Thread.sleep(3000);

        Assert.assertEquals(calp.getAssignedTo(), ConfigReader.get("loggedInUserName"), "Wrong default user");
        Reporter.log("Assigned To is the logged in user: " + calp.getAssignedTo(), true);
        calp.clickCancel();
    }

    @Test(priority = 7, groups = {"Integration", "Positive"}, description = "TC17 Create event assigned to another user")
    public void assignToAnotherUser_Test() throws Exception {

        String name = eu.getDataFromExcel("Calendar", 2, 1) + "_" + System.currentTimeMillis() % 100000;
        String user = eu.getDataFromExcel("Calendar", 2, 2);
        LocalDate nextMonth = LocalDate.now().plusMonths(1);

        calp.openCalendar();
        calp.openAddPopup("Meeting");
        calp.enterEventName(name);
        calp.setDates(nextMonth, nextMonth);
        calp.selectAssignedTo(user);
        Thread.sleep(3000);

        Assert.assertEquals(calp.getAssignedTo(), user, "Assigned To was not changed");
        calp.clickSave();

        String alert = wu.getAlertText(driver, 2);
        Assert.assertNull(alert, "Unexpected alert: " + alert);
        Assert.assertTrue(calp.isPopupClosed(), "Popup did not close after Save");
        Thread.sleep(3000);

        Reporter.log("Meeting " + name + " assigned to user: " + user, true);
    }

    @Test(priority = 8, groups = {"Functional", "Positive"}, description = "TC18 Add a user in the Invite tab")
    public void addInviteUser_Test() throws Exception {

        String user = eu.getDataFromExcel("Calendar", 3, 3);

        calp.openCalendar();
        calp.openAddPopup("Meeting");
        Thread.sleep(3000);

        calp.addInviteUser(user);
        Thread.sleep(3000);

        Assert.assertTrue(calp.getInvitedUsers().contains(user), "User was not added");
        Reporter.log("User added in the Invite tab: " + user, true);
        calp.clickCancel();
    }

    @Test(priority = 9, groups = {"Functional", "Positive"}, description = "TC19 Remove a user from the Invite tab")
    public void removeInviteUser_Test() throws Exception {

        String user = eu.getDataFromExcel("Calendar", 3, 3);

        calp.openCalendar();
        calp.openAddPopup("Meeting");
        calp.addInviteUser(user);
        Reporter.log("User added in the Invite tab: " + user, true);
        Thread.sleep(3000);

        calp.removeInviteUser(user);
        Thread.sleep(3000);

        Assert.assertFalse(calp.getInvitedUsers().contains(user), "User was not removed");
        Reporter.log("User removed from the Invite tab: " + user, true);
        calp.clickCancel();
    }
}