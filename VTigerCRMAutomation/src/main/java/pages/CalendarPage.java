package pages;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import utilities.ConfigReader;
import utilities.WebDriverUtility;

public class CalendarPage {

    WebDriver driver;
    WebDriverUtility wu = new WebDriverUtility();
    String url = ConfigReader.get("url");

    By totalEvents = By.xpath("//*[contains(text(),'My Total Events')]");
    By addButton = By.xpath(
            "//td[normalize-space(text())='Add'] | //div[normalize-space(text())='Add'] | //a[normalize-space(text())='Add']");

    // Add Event popup
    By eventName = By.xpath("//*[contains(normalize-space(text()),'Event Name')]/following::input[@type='text'][1]");
    By dateBoxes = By.xpath(
            "//input[@type='text' and string-length(@value)=10 and substring(@value,5,1)='-' and substring(@value,8,1)='-']");
    By assignedToByName = By.xpath("//select[@name='assigned_user_id']");
    By assignedToByOptions = By.xpath(
            "//select[not(@multiple) and option[normalize-space()='Bill Gates'] and option[normalize-space()='ACOE Fireflink']]");

    // the two invite lists are found with the position of the 'Add >>' button
    By addUserButton = By.xpath("//input[contains(@value,'Add >>')]");
    By removeUserButton = By.xpath("//input[contains(@value,'Add >>')]/following::input[contains(@value,'Remove')][1]");
    By availableUsers = By.xpath("//input[contains(@value,'Add >>')]/preceding::select[1]");
    By selectedUsers = By.xpath("//input[contains(@value,'Add >>')]/following::select[1]");

    By saveButton = By.xpath("//input[@value='Save' and (@type='button' or @type='submit')]");
    By cancelButton = By.xpath("//input[@value='Cancel' and (@type='button' or @type='submit')]");

    public CalendarPage(WebDriver driver) {
        this.driver = driver;
    }

    public void openCalendar() {
        wu.openUrl(driver, url + "index.php?module=Calendar&action=index");
    }

    public boolean isCalendarLoaded() {
        return wu.isVisible(driver, totalEvents, 10);
    }

    // the Call / Meeting / To Do option of the Add menu
    public By menuOption(String name) {
        return By.xpath("//a[normalize-space()='" + name + "'] | //td[normalize-space(text())='" + name
                + "'] | //div[normalize-space(text())='" + name + "']");
    }

    public void hoverAddButton() {
        WebElement add = wu.getVisible(driver, addButton);
        new Actions(driver).moveToElement(add).perform();
    }

    public boolean isAddOptionDisplayed(String name) {
        return wu.isVisible(driver, menuOption(name), 3);
    }

    // type = Call / Meeting / To Do
    public void openAddPopup(String type) {
        hoverAddButton();
        if (!wu.isVisible(driver, menuOption(type), 2)) {
            wu.click(driver, addButton);
        }
        wu.click(driver, menuOption(type));
        wu.getVisible(driver, eventName);
    }

    public void enterEventName(String name) {
        wu.type(driver, eventName, name);
    }

    // writes the start and end date (yyyy-MM-dd) into the two date boxes
    public void setDates(LocalDate start, LocalDate end) {
        wu.getVisible(driver, dateBoxes);
        List<WebElement> boxes = new ArrayList<WebElement>();
        for (WebElement e : driver.findElements(dateBoxes)) {
            if (e.isDisplayed()) {
                boxes.add(e);
            }
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = arguments[1];", boxes.get(0), start.toString());
        js.executeScript("arguments[0].value = arguments[1];", boxes.get(1), end.toString());
    }

    public Select getAssignedToList() {
        if (wu.isVisible(driver, assignedToByName, 3)) {
            return new Select(wu.getVisible(driver, assignedToByName));
        }
        return new Select(wu.getVisible(driver, assignedToByOptions));
    }

    public String getAssignedTo() {
        return getAssignedToList().getFirstSelectedOption().getText().trim();
    }

    public void selectAssignedTo(String user) {
        getAssignedToList().selectByVisibleText(user);
    }

    public void addInviteUser(String user) {
        new Select(wu.getVisible(driver, availableUsers)).selectByVisibleText(user);
        wu.click(driver, addUserButton);
    }

    public void removeInviteUser(String user) {
        new Select(wu.getVisible(driver, selectedUsers)).selectByVisibleText(user);
        wu.click(driver, removeUserButton);
    }

    public List<String> getInvitedUsers() {
        List<String> names = new ArrayList<String>();
        for (WebElement option : new Select(wu.getVisible(driver, selectedUsers)).getOptions()) {
            names.add(option.getText().trim());
        }
        return names;
    }

    public void clickSave() {
        wu.click(driver, saveButton);
    }

    public void clickCancel() {
        wu.click(driver, cancelButton);
    }

    public boolean isPopupClosed() {
        return wu.isGone(driver, eventName, 15);
    }
}
