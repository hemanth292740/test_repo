package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;





public class OrangeHRMLoginPage {

	static By OrangeHRM_usernameField = By.xpath("//input[@name='username']");
	static By OrangeHRM_passwordField = By.xpath("//input[@name='password']");
	static By OrangeHRM_Login = By.xpath("//*[@type='submit']");
	static By OrangeHRM_HeaderUserAreaDd = By.xpath("(//div[@class='oxd-topbar-header-userarea']//li)[1]");
	static By OrangeHRM_ClickonLogOutButton = By.xpath("(//div[@class='oxd-topbar-header-userarea']//li)[5]");
//	static WebDriver driver = Repository.driver;

	static By OrangeHRM_DashboardHeader = By.xpath("//h6[text()='Dashboard']");

	public static void Login(WebDriver driver) throws InterruptedException {

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		wait.until(ExpectedConditions.visibilityOfElementLocated(OrangeHRM_usernameField)).sendKeys("Admin");
		driver.findElement(OrangeHRM_passwordField).sendKeys("admin123");
		driver.findElement(OrangeHRM_Login).click();

		// Without this, the caller's next capture fires immediately after the click,
		// before the SPA has navigated to /dashboard/index at all -- confirmed live:
		// both the baseline and every subsequent run captured a blank page here.
		wait.until(ExpectedConditions.visibilityOfElementLocated(OrangeHRM_DashboardHeader));
	}

	public static void Logout(WebDriver driver) throws Exception {

//		String CurrentMethod = ReportExtract.getCurrentMethodName();
//		System.out.println("Method name: " + CurrentMethod);
//		GenericMethod.getModuleName(CurrentMethod);

		Actions action = new Actions(driver);

		try {
			Thread.sleep(3000);
			driver.findElement(OrangeHRM_HeaderUserAreaDd).click();
			Thread.sleep(3000);
			driver.findElement(OrangeHRM_ClickonLogOutButton).click();
			Thread.sleep(3000);
//			ReportExtract.UpdateTestCaseModuleStatus("Pass", CurrentMethod);
		} catch (Exception e) {
			System.out.println(e.getMessage().toString());
//			ReportExtract.UpdateTestCaseModuleStatus("Fail", CurrentMethod);
		}

	}

}
