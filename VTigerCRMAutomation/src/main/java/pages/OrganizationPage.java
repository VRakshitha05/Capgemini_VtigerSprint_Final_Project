package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import utilities.ConfigReader;
import utilities.WebDriverUtility;

public class OrganizationPage {

    WebDriver driver;
    WebDriverUtility wu = new WebDriverUtility();
    String url = ConfigReader.get("url");

    By orgName = By.name("accountname");
    By website = By.name("website");
    By phone = By.name("phone");
    By email = By.name("email1");
    By employees = By.name("employees");
    By saveButton = By.xpath(
            "//input[@type='submit' and contains(@title,'Save')] | //input[@name='button' and contains(@value,'Save')]");

    By searchField = By.xpath(
            "//select[@id='bas_searchfield' or @name='search_field'] | "
            + "//select[option[normalize-space()='Organization Name'] and option[normalize-space()='Organization No']]");
    By searchText = By.name("search_text");
    By searchNow = By.xpath("//input[contains(@value,'Search Now')]");
    By deleteButton = By.xpath("//input[@type='button' and contains(@value,'Delete')]");

    public OrganizationPage(WebDriver driver) {
        this.driver = driver;
    }

    public void openList() {
        wu.openUrl(driver, url + "index.php?module=Accounts&action=index");
        wu.getVisible(driver, searchText);
    }

    public boolean isListLoaded() {
        return wu.isVisible(driver, searchText, 10) && driver.getPageSource().contains("Showing Records");
    }

    public void openCreateForm() {
        wu.openUrl(driver, url + "index.php?module=Accounts&action=EditView&return_action=DetailView&parenttab=Marketing");
        wu.getVisible(driver, orgName);
    }

    public void fillForm(String name, String site, String phoneNo, String mail, String emp) {
        wu.type(driver, orgName, name);
        wu.type(driver, website, site);
        wu.type(driver, phone, phoneNo);
        wu.type(driver, email, mail);
        wu.type(driver, employees, emp);
    }

    public void changeName(String newName) {
        wu.type(driver, orgName, newName);
    }

    public void clickSave() {
        wu.click(driver, saveButton);
    }

    public boolean isOnEditForm() {
        return driver.getCurrentUrl().contains("action=EditView") && wu.isVisible(driver, orgName, 3);
    }

    // after saving an edit, the browser leaves the edit page
    public void waitEditSaved() {
        for (int i = 0; i < 30; i++) {
            if (!driver.getCurrentUrl().contains("&action=EditView")) {
                return;
            }
            wu.waitFor(500);
        }
    }

    // true when the saved record page is open and shows the name
    // (the create form url has "return_action=DetailView", so we look for "&action=" and "?action=")
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

    public void search(String name) {
        new Select(wu.getVisible(driver, searchField)).selectByVisibleText("Organization Name");
        wu.type(driver, searchText, name);
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

    // waits up to 12 seconds for the name to disappear from the list
    public boolean isGone(String name) {
        return wu.isGone(driver, nameLink(name), 12);
    }

    // 'edit' link in the Action column
    public void clickEditInRow(String name) {
        wu.click(driver, By.xpath(row(name) + "//a[normalize-space(text())='edit']"));
        wu.getVisible(driver, orgName);
    }

    // 'del' link in the Action column
    public void deleteInRow(String name) {
        wu.click(driver, By.xpath(row(name) + "//a[normalize-space(text())='del']"));
        wu.getAlertText(driver, 5);
        wu.waitFor(2000);
    }

    public void clickDeleteButton() {
        wu.click(driver, deleteButton);
    }
}
