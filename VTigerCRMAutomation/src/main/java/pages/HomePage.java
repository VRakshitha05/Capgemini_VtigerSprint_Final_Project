package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import utilities.ConfigReader;
import utilities.WebDriverUtility;

public class HomePage {

    WebDriver driver;
    WebDriverUtility wu = new WebDriverUtility();
    String url = ConfigReader.get("url");

    By plusIcon = By.xpath("//img[contains(@onclick,'fnAddWindow') or contains(@onmouseover,'fnAddWindow')]");
    By moduleOption = By.id("addmodule");
    By dashboardOption = By.id("adddash");
    By notebookOption = By.id("addNotebook");

    By windowTitle = By.id("stufftitle_id");
    By saveButton = By.id("savebtn");
    By dashboardName = By.id("seldashbd_id");
    By dashboardType = By.id("seldashtype_id");
    By fieldsToShow = By.xpath(
            "//select[option[normalize-space()='Organization No'] and option[normalize-space()='Organization Name']]");

    public HomePage(WebDriver driver) {
        this.driver = driver;
    }

    public void openHome() {
        wu.openUrl(driver, url + "index.php?module=Home&action=index");
        wu.getVisible(driver, plusIcon);
    }

    public boolean isMenuDisplayed(String menuName) {
        return wu.isVisible(driver, By.linkText(menuName), 5);
    }

    // clicks the + icon and the option, the popup is loaded from the server so it gets 30 seconds
    private void openPopup(By option) {
        wu.click(driver, plusIcon);
        wu.click(driver, option);
        wu.getVisible(driver, windowTitle, 30);
    }

    public void openModulePopup() {
        openPopup(moduleOption);
    }

    public void openDashboardPopup() {
        openPopup(dashboardOption);
    }

    public void openNotebookPopup() {
        openPopup(notebookOption);
    }

    public void enterTitle(String title) {
        wu.type(driver, windowTitle, title);
    }

    public void selectFirstTwoFields() {
        Select fields = new Select(wu.getVisible(driver, fieldsToShow));
        fields.selectByIndex(0);
        fields.selectByIndex(1);
    }

    public void selectDashboardByIndex(int nameIndex, int typeIndex) {
        new Select(wu.getVisible(driver, dashboardName)).selectByIndex(nameIndex);
        new Select(wu.getVisible(driver, dashboardType)).selectByIndex(typeIndex);
    }

    public void clickSave() {
        wu.click(driver, saveButton);
    }

    public boolean isPopupClosed() {
        return wu.isGone(driver, windowTitle, 15);
    }

    public boolean isTitleOnHome(String title) {
        return wu.isVisible(driver, By.xpath("//*[contains(normalize-space(text()),'" + title + "')]"), 10);
    }
}
