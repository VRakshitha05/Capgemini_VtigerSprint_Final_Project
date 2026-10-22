package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import utilities.ConfigReader;
import utilities.WebDriverUtility;

public class ContactPage {

    WebDriver driver;
    WebDriverUtility wu = new WebDriverUtility();
    String url = ConfigReader.get("url");

    // create form
    By lastName = By.name("lastname");
    By firstName = By.name("firstname");
    By title = By.name("title");
    By email = By.name("email");
    By orgLookupIcon = By.xpath("//input[@name='account_name']/following::img[1]");
    By saveButton = By.xpath(
            "//input[@type='submit' and contains(@title,'Save')] | //input[@name='button' and contains(@value,'Save')]");

    // list page
    By searchField = By.xpath(
            "//select[@id='bas_searchfield' or @name='search_field'] | "
            + "//select[option[normalize-space()='Contact Id'] and option[normalize-space()='Last Name']]");
    By searchText = By.name("search_text");
    By searchNow = By.xpath("//input[contains(@value,'Search Now')]");
    By massEditButton = By.xpath("//input[@type='button' and contains(@value,'Mass Edit')]");
    By sendMailButton = By.xpath("//input[@type='button' and contains(@value,'Send Mail')]");

    // organization popup window
    By popupSearchField = By.xpath("//select[@id='bas_searchfield' or @name='search_field']");

    // mass edit popup
    By massEditHeading = By.xpath("//*[contains(text(),'Mass Edit - Records Fields')]");
    By titleCheckbox = By.xpath(
            "//input[@id='title_mass_edit_check'] | //td[normalize-space(.)='Title']//input[@type='checkbox']");
    By massTitleBox = By.xpath("//input[@name='title' and @type='text']");
    By massSaveButton = By.xpath("//input[contains(@value,'Save') and (@type='button' or @type='submit')]");

    // "Select Email IDs" popup and compose window
    By selectEmailHeading = By.xpath("//*[contains(text(),'Select Email IDs')]");
    By selectEmailButton = By.xpath("//input[normalize-space(@value)='Select']");
    By subjectBox = By.xpath("//input[@name='subject']");

    public ContactPage(WebDriver driver) {
        this.driver = driver;
    }

    public void openList() {
        wu.openUrl(driver, url + "index.php?module=Contacts&action=index");
        wu.getVisible(driver, searchText);
    }

    public boolean isListLoaded() {
        return wu.isVisible(driver, searchText, 10) && driver.getPageSource().contains("Showing Records");
    }

    public void openCreateForm() {
        wu.openUrl(driver, url + "index.php?module=Contacts&action=EditView&return_action=DetailView&parenttab=Marketing");
        wu.getVisible(driver, lastName);
    }

    public void fillForm(String last, String first, String titleText, String mail) {
        wu.type(driver, lastName, last);
        wu.type(driver, firstName, first);
        wu.type(driver, title, titleText);
        wu.type(driver, email, mail);
    }

    // opens the organization popup window, searches the organization and selects it
    // (if the popup shows an error or the organization is not found, it is closed and tried again)
    public void selectOrganization(String orgName) {
        String mainWindow = driver.getWindowHandle();

        for (int attempt = 1; attempt <= 3; attempt++) {
            wu.click(driver, orgLookupIcon);
            wu.switchToNewWindow(driver, mainWindow);

            if (!driver.getPageSource().contains("Too many connections")) {
                if (wu.isVisible(driver, popupSearchField, 5)) {
                    new Select(wu.getVisible(driver, popupSearchField)).selectByVisibleText("Organization Name");
                }
                wu.type(driver, searchText, orgName);
                wu.click(driver, searchNow);
                wu.waitFor(2500);

                if (wu.isVisible(driver, nameLink(orgName), 10)) {
                    wu.click(driver, nameLink(orgName));
                    wu.waitFor(1500);
                    driver.switchTo().window(mainWindow);
                    return;
                }
            }

            System.out.println("Organization popup not ready, trying again " + attempt + " of 3");
            driver.close();
            driver.switchTo().window(mainWindow);
            wu.waitFor(10000);
        }
        throw new RuntimeException("Organization not found in the popup: " + orgName);
    }

    public void clickSave() {
        wu.click(driver, saveButton);
    }

    public boolean isOnEditForm() {
        return driver.getCurrentUrl().contains("action=EditView") && wu.isVisible(driver, lastName, 3);
    }

    // true when the saved record page is open and shows the name
    public boolean isDetailPage(String name) {
        for (int i = 0; i < 30; i++) {
            String currentUrl = driver.getCurrentUrl();
            boolean detail = currentUrl.contains("&action=DetailView") || currentUrl.contains("?action=DetailView");
            if (detail && driver.getPageSource().contains(name)) {
                return true;
            }
            wu.waitFor(500);
        }
        return false;
    }

    // ---------- list page ----------
    public void search(String last) {
        new Select(wu.getVisible(driver, searchField)).selectByVisibleText("Last Name");
        wu.type(driver, searchText, last);
        wu.click(driver, searchNow);
        wu.waitFor(2500);
    }

    public By nameLink(String name) {
        return By.xpath("//a[normalize-space(text())='" + name + "']");
    }

    public String row(String name) {
        return "//a[normalize-space(text())='" + name + "']/ancestor::tr[1]";
    }

    public boolean isInList(String name) {
        return wu.isVisible(driver, nameLink(name), 8);
    }

    public boolean isGone(String name) {
        return wu.isGone(driver, nameLink(name), 12);
    }

    // true when the row of this contact shows the given text (organization name, title ...)
    public boolean isTextInRow(String name, String text) {
        return wu.isVisible(driver, By.xpath(row(name) + "//*[normalize-space(text())='" + text + "']"), 8);
    }

    public void tickRow(String name) {
        wu.click(driver, By.xpath(row(name) + "//input[@type='checkbox']"));
    }

    // 'del' link in the Action column
    public void deleteInRow(String name) {
        wu.click(driver, By.xpath(row(name) + "//a[normalize-space(text())='del']"));
        wu.getAlertText(driver, 5);
        wu.waitFor(2000);
    }

    // ---------- mass edit ----------
    public void clickMassEdit() {
        wu.click(driver, massEditButton);
        wu.getVisible(driver, massEditHeading);
    }

    // ticks the checkbox next to Title, types the new title and saves
    public void massEditTitle(String newTitle) {
        wu.click(driver, titleCheckbox);
        wu.type(driver, massTitleBox, newTitle);
        wu.waitFor(1500);
        wu.click(driver, massSaveButton);
        wu.waitFor(2500);
    }

    // ---------- send mail ----------
    public void clickSendMail() {
        wu.click(driver, sendMailButton);
    }

    public boolean isSelectEmailPopupDisplayed() {
        return wu.isVisible(driver, selectEmailHeading, 10);
    }

    public void clickSelectEmail() {
        wu.click(driver, selectEmailButton);
    }

    // the Compose E-Mail window is loaded from the server, so it gets 30 seconds
    public boolean isComposeWindowOpen() {
        return wu.isVisible(driver, subjectBox, 30) && driver.getTitle().contains("Compose");
    }

    public void enterSubject(String subject) {
        wu.type(driver, subjectBox, subject);
    }

    public String getSubject() {
        return wu.getVisible(driver, subjectBox).getAttribute("value");
    }
}
