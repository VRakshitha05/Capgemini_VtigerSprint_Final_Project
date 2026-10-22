package base;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
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

	// static: one browser is opened for each <test> and shared by all its test
	// classes
	public static WebDriver driver;

	// page objects, created here so the test classes do not need any setup code
	public LoginPage lp;
	public HomePage hp;
	public CalendarPage calp;
	public ContactPage cp;
	public OrganizationPage op;

	@BeforeTest(alwaysRun = true)
	@Parameters("browser")
	public void launchBrowser(@Optional("chrome") String browser) {

		if (ConfigReader.get("password").equals("CHANGE_ME")) {
			throw new RuntimeException("Set your real password in src/main/resources/config.properties");
		}

		if (browser.equalsIgnoreCase("chrome")) {

			ChromeOptions options = new ChromeOptions();
			options.addArguments("--disable-notifications");

			Map<String, Object> prefs = new HashMap<String, Object>();
			prefs.put("credentials_enable_service", false);
			prefs.put("profile.password_manager_enabled", false);
			prefs.put("profile.password_manager_leak_detection", false);
			options.setExperimentalOption("prefs", prefs);

			driver = new ChromeDriver(options);

		} else if (browser.equalsIgnoreCase("firefox")) {

			driver = new FirefoxDriver();

		} else if (browser.equalsIgnoreCase("edge")) {

			driver = new EdgeDriver();

		} else {
			throw new RuntimeException("Invalid browser: " + browser);
		}

		driver.manage().window().maximize();
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
		driver.get(ConfigReader.get("url"));

		Reporter.log("Browser opened: " + browser, true);
	}

	// runs once for every test class: creates the page objects and logs in
	@BeforeClass(alwaysRun = true)
	public void login() {
		lp = new LoginPage(driver);
		hp = new HomePage(driver);
		calp = new CalendarPage(driver);
		cp = new ContactPage(driver);
		op = new OrganizationPage(driver);

		loginToApplication();
	}

	// runs before every test method, but only does something for LoginTest.
	// It makes sure the login page is really on the screen. It never throws.
	@BeforeMethod(alwaysRun = true)
	public void prepareLoginPage() {
		if (!getClass().getSimpleName().equals("LoginTest")) {
			return;
		}

		try {
			// already on the login page, nothing to do
			if (isLoginPageNow()) {
				return;
			}

			// 1st try: normal logout, then WAIT so the logout request can finish
			try {
				lp.logout();
				Thread.sleep(3000);
			} catch (Exception e) {
				Reporter.log("prepareLoginPage: logout click failed: " + e.getMessage(), true);
			}
			lp.openLoginPage();
			if (waitForLoginPage(10)) {
				return;
			}

			// 2nd try: remove the session cookies
			driver.manage().deleteAllCookies();
			driver.get(ConfigReader.get("url"));
			if (waitForLoginPage(10)) {
				return;
			}

			// 3rd try: call the Vtiger logout URL directly
			driver.get(ConfigReader.get("url") + "index.php?module=Users&action=Logout");
			Thread.sleep(3000);
			driver.manage().deleteAllCookies();
			driver.get(ConfigReader.get("url"));
			if (!waitForLoginPage(10)) {
				Reporter.log("prepareLoginPage problem: login page is still not displayed", true);
			}
		} catch (Exception e) {
			Reporter.log("prepareLoginPage problem: " + e, true);
		}
	}

	// checks the login page once, without waiting
	private boolean isLoginPageNow() {
		try {
			return lp.isLoginPageDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

	// checks the logged in state once, without throwing
	private boolean isLoggedInNow() {
		try {
			return lp.isLoggedIn();
		} catch (Exception e) {
			return false;
		}
	}

	// logs in with up to 3 attempts. Each attempt waits for the login page,
	// and a failed attempt clears the cookies so the next one starts clean.
	public void loginToApplication() {

		String lastProblem = "no problem recorded";

		for (int attempt = 1; attempt <= 3; attempt++) {
			try {
				lp.openLoginPage();

				// the session may already be active (Home page opened instead of login page)
				if (isLoggedInNow()) {
					Reporter.log("Logged in to Vtiger", true);
					return;
				}

				if (!waitForLoginPage(20)) {
					lastProblem = "login page did not appear";
					Reporter.log("Login attempt " + attempt + " failed: " + lastProblem, true);
					driver.manage().deleteAllCookies();
					continue;
				}

				lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
				Thread.sleep(3000);

				if (isLoggedInNow()) {
					Reporter.log("Logged in to Vtiger", true);
					return;
				}

				lastProblem = "Home page was not displayed after entering the credentials";
				Reporter.log("Login attempt " + attempt + " failed: " + lastProblem, true);
				driver.manage().deleteAllCookies();

			} catch (Exception e) {
				lastProblem = e.toString();
				Reporter.log("Login attempt " + attempt + " failed: " + lastProblem, true);
				try {
					driver.manage().deleteAllCookies();
				} catch (Exception ignored) {
					// browser problem, the next attempt will show it
				}
			}
		}

		Assert.fail("Login failed after 3 attempts (" + lastProblem
				+ ") - check the url, username and password in config.properties");
	}

	// waits up to the given seconds for the login page to appear
	public boolean waitForLoginPage(int seconds) {
		for (int i = 0; i < seconds; i++) {
			try {
				if (lp.isLoginPageDisplayed()) {
					return true;
				}
			} catch (Exception e) {
				// page still loading, try again
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return false;
	}

	// saves a screenshot in the "screenshots" folder (used by the negative tests)
	public void takeScreenshot(String fileName) {
		try {
			File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			Files.createDirectories(Paths.get("screenshots"));
			Path target = Paths.get("screenshots", fileName + ".png");
			Files.copy(src.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
			Reporter.log("Screenshot saved: " + target.toAbsolutePath(), true);
		} catch (Exception e) {
			Reporter.log("Could not take screenshot: " + e.getMessage(), true);
		}
	}

	@AfterTest(alwaysRun = true)
	public void closeBrowser() {
		if (driver != null) {
			driver.quit();
			Reporter.log("Browser closed", true);
		}
	}
}