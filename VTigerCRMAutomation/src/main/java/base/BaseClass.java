
package base;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import pages.CalendarPage;
import pages.ContactPage;
import pages.HomePage;
import pages.LoginPage;
import pages.OrganizationPage;
import utilities.ConfigReader;

public class BaseClass {

    public static WebDriver driver;

    public LoginPage lp;
    public HomePage hp;
    public CalendarPage calp;
    public ContactPage cp;
    public OrganizationPage op;

    @BeforeTest(alwaysRun = true)
    @Parameters("browser")
    public void launchBrowser(@Optional("chrome") String browser) {

        String password = ConfigReader.get("password");

        if (password == null || password.trim().isEmpty()
                || password.equals("CHANGE_ME")) {
            throw new RuntimeException(
                "Set your valid password in config.properties"
            );
        }

        if (browser.equalsIgnoreCase("chrome")) {

            ChromeOptions options = new ChromeOptions();
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);
            options.addArguments("--disable-notifications");

            Map<String, Object> prefs = new HashMap<>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            prefs.put("profile.password_manager_leak_detection", false);
            options.setExperimentalOption("prefs", prefs);

            driver = new ChromeDriver(options);

        } else if (browser.equalsIgnoreCase("firefox")) {

            FirefoxOptions options = new FirefoxOptions();
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);
            driver = new FirefoxDriver(options);

        } else if (browser.equalsIgnoreCase("edge")) {

            EdgeOptions options = new EdgeOptions();
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);
            options.addArguments("--disable-notifications");
            driver = new EdgeDriver(options);

        } else {
            throw new RuntimeException("Unsupported browser: " + browser);
        }

        driver.manage().window().maximize();

        driver.manage().timeouts()
              .pageLoadTimeout(Duration.ofSeconds(45));

        driver.manage().timeouts()
              .scriptTimeout(Duration.ofSeconds(30));

        driver.get(ConfigReader.get("url"));

        Reporter.log("Browser opened: " + browser, true);
    }

    @BeforeClass(alwaysRun = true)
    public void initializePagesAndLogin() {

        lp = new LoginPage(driver);
        hp = new HomePage(driver);
        calp = new CalendarPage(driver);
        cp = new ContactPage(driver);
        op = new OrganizationPage(driver);

        if (!getClass().getSimpleName().equals("LoginTest")) {
            loginToApplication();
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareLoginTest() {

        if (!getClass().getSimpleName().equals("LoginTest")) {
            return;
        }

        try {
            if (lp.isLoginPageDisplayed()) {
                return;
            }
        } catch (Exception ignored) {
            // Open the login page below.
        }

        lp.openLoginPage();

        if (!waitForLoginPage(15)) {
            driver.manage().deleteAllCookies();
            lp.openLoginPage();
        }
    }

    public void loginToApplication() {

        String username = ConfigReader.get("username");
        String password = ConfigReader.get("password");
        String lastError = "Login page or home page was not displayed";

        for (int attempt = 1; attempt <= 3; attempt++) {

            try {
                if (lp.isLoggedIn()) {
                    Reporter.log("Already logged in to VTiger.", true);
                    return;
                }

                lp.openLoginPage();

                if (!waitForLoginPage(15)) {
                    lastError = "Login page did not appear";
                    driver.manage().deleteAllCookies();
                    continue;
                }

                lp.login(username, password);

                if (lp.waitForLoggedIn(20)) {
                    Reporter.log("Logged in to VTiger.", true);
                    return;
                }

                lastError = "Home page did not appear after login";

            } catch (Exception e) {
                lastError = e.getMessage();
                Reporter.log(
                    "Login attempt " + attempt + " failed: " + lastError,
                    true
                );
            }

            try {
                driver.manage().deleteAllCookies();
            } catch (Exception ignored) {
                // Continue to the next attempt.
            }
        }

        Assert.fail("VTiger login failed: " + lastError);
    }

    public boolean waitForLoginPage(int seconds) {

        long end = System.currentTimeMillis() + seconds * 1000L;

        while (System.currentTimeMillis() < end) {
            try {
                if (lp.isLoginPageDisplayed()) {
                    return true;
                }
            } catch (Exception ignored) {
                // Page may still be loading.
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }

    public void takeScreenshot(String fileName) {

        try {
            File source = ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.FILE);

            Files.createDirectories(Paths.get("screenshots"));

            Path target = Paths.get("screenshots", fileName + ".png");

            Files.copy(
                source.toPath(),
                target,
                StandardCopyOption.REPLACE_EXISTING
            );

            Reporter.log(
                "Screenshot saved: " + target.toAbsolutePath(),
                true
            );

        } catch (Exception e) {
            Reporter.log(
                "Screenshot failed: " + e.getMessage(),
                true
            );
        }
    }

    @AfterTest(alwaysRun = true)
    public void closeBrowser() {

        if (driver != null) {
            driver.quit();
            driver = null;
            Reporter.log("Browser closed.", true);
        }
    }
}
