package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class OrangeHRMDashboard {

	static By OrangeHRM_Admin = By
			.xpath("//div[@class='oxd-sidepanel-body']//li[1]//a[@href='/web/index.php/admin/viewAdminModule']");
	// Edit button of the row whose Username column (cell 2) is exactly "Admin" -- NOT the
	// grid's first row. The public OrangeHRM demo is shared, other people add/delete users
	// all the time, so row [1] drifted from user 1 to user 111 between two runs (confirmed
	// live, 2026-09-28). Visual baselines are keyed by sha256(pageUrl), so a different user
	// = a different /admin/saveSystemUser/{id} URL = "No baseline set". Admin is the account
	// this script logs in with, so it's the one user that stays put. Matches cell 2 only --
	// "Admin" also appears in many rows' User Role column (cell 3).
	static By OrangeHRM_AdminGrid_UserDetailsEditButton = By
			.xpath("//div[@class='oxd-table-body']//div[@class='oxd-table-card']"
					+ "[.//div[@role='cell'][2][normalize-space()='Admin']]//button[2]");
	static By OrangeHRM_AdminGrid_EditUserStatusDropDown = By.xpath("(//div[@class='oxd-select-text--after'])[2]");
	static By OrangeHRM_AdminGrid_StatusDisableOption = By.xpath("(//div[@role='listbox'])//div[3]");
	static By OrangeHRM_AdminGrid_StatusEnableOption = By.xpath("(//div[@role='listbox'])//div[2]");
	static By OrangeHRM_AdminGrid_EditUserSaveButton = By.xpath("(//div[@class='oxd-form-actions']//button)[2]");
	static By OrangeHRM_TimeSheet = By
			.xpath("(//li[@class='oxd-main-menu-item-wrapper'])[4]//a[@href='/web/index.php/time/viewTimeModule']");
	static By OrangeHRM_EmployeeNameinTimeSheet = By.xpath("//div[@class='oxd-autocomplete-wrapper']//input");
	static By OrangeHRM_TimeSheetViewButton = By.xpath("//div[@class='oxd-form-actions']//button");
	static By OrangeHRM_TimesheetSubmitButton = By.xpath("//div[@class='orangehrm-timesheet-footer']//button[2]");
	static By OrangeHRM_CreateTimeSheetButton = By.xpath("//div[@class='orangehrm-timesheet-footer']//button");

	static By OrangeHRM_TimeSheetEditButton = By.xpath("//div[@class='orangehrm-timesheet-footer']//button[1]");

	static By OrangeHRM_EnterProjectDetails = By.xpath("//div[@class='oxd-autocomplete-wrapper']//input");
	static By OrangeHRM_ActivityDropDownArrow = By.xpath("(//div[@class='oxd-select-text--after'])[1]");
	static By OrangeHRM_TimesheetSaveButton = By.xpath("//div[@class='orangehrm-timesheet-footer']//button[3]");

	static By OrangeHRM_TimeSheetSubmittedStatus = By.xpath("(//p[text()='Status: Submitted']/following::button)[1]");
	//static WebDriver driver = Repository.driver;

	public static void dashboard(String Value,WebDriver driver) throws Exception {

//		String CurrentMethod = ReportExtract.getCurrentMethodName();
//		System.out.println("Method name: " + CurrentMethod);
//		GenericMethod.getModuleName(CurrentMethod);

		try {
			if (Value.equalsIgnoreCase("admin")) {
				Thread.sleep(3000);

				driver.findElement(OrangeHRM_Admin).click();
				// Wait for the Admin > User Management grid to actually render before
				// returning -- without this, the caller's next capture (accessibility/visual)
				// can fire before the SPA route change finishes, capturing the previous
				// (dashboard) page instead of this one.
				new WebDriverWait(driver, Duration.ofSeconds(15))
						.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='oxd-table-body']")));
			} else if (Value.equalsIgnoreCase("timesheet")) {
				Thread.sleep(3000);
				driver.findElement(OrangeHRM_TimeSheet).click();
			}
//			ReportExtract.UpdateTestCaseModuleStatus("Pass", CurrentMethod);
		} catch (Exception e) {
			System.out.println(e.getMessage().toString());
//			ReportExtract.UpdateTestCaseModuleStatus("Fail", CurrentMethod);
		}

	}

	public static void ClickonAdmin(WebDriver driver) throws Exception {
//		String CurrentMethod = ReportExtract.getCurrentMethodName();
//		System.out.println("Method name: " + CurrentMethod);
//		GenericMethod.getModuleName(CurrentMethod);
		try {

			Thread.sleep(3000);
			scrollby(20, 450,driver);
			Thread.sleep(3000);

			WebElement editButton;
			try {
				editButton = new WebDriverWait(driver, Duration.ofSeconds(15))
						.until(ExpectedConditions.visibilityOfElementLocated(OrangeHRM_AdminGrid_UserDetailsEditButton));
			} catch (org.openqa.selenium.TimeoutException e) {
				// Fail loudly: rethrown past this method's catch-all below, so the runner stops
				// instead of capturing whatever page it's on as "edituserpage".
				throw new IllegalStateException("Admin user row not found in the Admin > User Management grid", e);
			}
			editButton.click();

			// Attempt 1 (confirmed broken live via S3: baseline AND current both froze on
			// the loading spinner, run after run) was invisibilityOfElementLocated(spinner)
			// alone. That's a no-op here: at the instant this wait starts, right after the
			// click, the spinner div (confirmed present in the real DOM snapshot at
			// class="oxd-loading-spinner") hasn't been mounted by Vue yet -- Selenium
			// treats "not present in the DOM" as "invisible", so the wait was satisfied
			// immediately, before the spinner even appeared, let alone went away.
			By spinner = By.className("oxd-loading-spinner");
			try {
				new WebDriverWait(driver, Duration.ofSeconds(3))
						.until(ExpectedConditions.visibilityOfElementLocated(spinner));
			} catch (org.openqa.selenium.TimeoutException ignored) {
				// Form rendered before we could even catch the spinner mid-flight -- fine.
			}
			new WebDriverWait(driver, Duration.ofSeconds(20))
					.until(ExpectedConditions.invisibilityOfElementLocated(spinner));

			// Also wait for an element that only exists once the real form has rendered --
			// the previous dropdown-visibility wait here used a locator
			// ((//div[@class='oxd-select-text--after'])[2]) that the PREVIOUS (Admin grid)
			// page's own "User Role"/"Status" filter dropdowns satisfied just as well, so it
			// passed regardless of whether this page had actually loaded. The Save button is
			// part of the Edit User form itself, not shared with the grid page.
			new WebDriverWait(driver, Duration.ofSeconds(15))
					.until(ExpectedConditions.visibilityOfElementLocated(OrangeHRM_AdminGrid_EditUserSaveButton));

			// Same URL every run is what lets the edituserpage visual baseline match.
			System.out.println("Edit User page URL: " + driver.getCurrentUrl());

			//ReportExtract.UpdateTestCaseModuleStatus("Pass", CurrentMethod);
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			System.out.println(e.getMessage().toString());
			//ReportExtract.UpdateTestCaseModuleStatus("Fail", CurrentMethod);
		}

	}

	public static void EditUserPage(WebDriver driver) throws Exception {
//		String CurrentMethod = ReportExtract.getCurrentMethodName();
//		System.out.println("Method name: " + CurrentMethod);
//		GenericMethod.getModuleName(CurrentMethod);
		try {
			driver.findElement(OrangeHRM_AdminGrid_EditUserStatusDropDown).click();
			Thread.sleep(3000);
			driver.findElement(OrangeHRM_AdminGrid_StatusDisableOption).click();
			Thread.sleep(3000);
			driver.findElement(OrangeHRM_AdminGrid_EditUserStatusDropDown).click();
			Thread.sleep(3000);
			driver.findElement(OrangeHRM_AdminGrid_StatusEnableOption).click();
			Thread.sleep(3000);
			driver.findElement(OrangeHRM_AdminGrid_EditUserSaveButton).click();
			Thread.sleep(3000);

			//ReportExtract.UpdateTestCaseModuleStatus("Pass", CurrentMethod);
		} catch (Exception e) {
			System.out.println(e.getMessage().toString());
			//ReportExtract.UpdateTestCaseModuleStatus("Fail", CurrentMethod);

		}

	}

	public static void ClickonTimeSheet(WebDriver driver) throws Exception {
//		String CurrentMethod = ReportExtract.getCurrentMethodName();
//		System.out.println("Method name: " + CurrentMethod);
//		GenericMethod.getModuleName(CurrentMethod);
		Actions action = new Actions(driver);
		try {

			Thread.sleep(3000);
			driver.findElement(OrangeHRM_EmployeeNameinTimeSheet).click();
			Thread.sleep(3000);
			driver.findElement(OrangeHRM_EmployeeNameinTimeSheet).sendKeys("O");
			Thread.sleep(3000);

			for (int i = 0; i < 2; i++) {

				action.sendKeys(Keys.DOWN);
				action.build().perform();
				Thread.sleep(3000);
			}
			Thread.sleep(3000);
			action.sendKeys(Keys.ENTER).build().perform();
			Thread.sleep(3000);
			driver.findElement(OrangeHRM_TimeSheetViewButton).click();
			Thread.sleep(3000);
			try {
				Thread.sleep(5000);
				WebElement g = driver.findElement(OrangeHRM_TimeSheetSubmittedStatus);
				String s = g.getText();
				System.out.println(s);
				// OrangeHRMLoginPage.Logout();
				Thread.sleep(2000);
			} catch (Exception e) {
				// System.out.println(e.getMessage().toString());

			}
			try {
				Thread.sleep(5000);
				WebElement g = driver.findElement(OrangeHRM_TimesheetSubmitButton);
				JavascriptExecutor js = (JavascriptExecutor) driver;
				js.executeScript("arguments[0].click();", g);
				Thread.sleep(5000);
				// OrangeHRMLoginPage.Logout();
				Thread.sleep(5000);
			}

			catch (Exception e) {

				try {
					WebElement CreateTimeSheetButton = driver.findElement(OrangeHRM_CreateTimeSheetButton);
					Thread.sleep(5000);
					javascriptexecutor(CreateTimeSheetButton, "javascriptExecutorClick",driver);

				} catch (Exception e1) {
					WebElement CreateTimeSheetButton1 = driver.findElement(OrangeHRM_TimeSheetEditButton);
					Thread.sleep(5000);
					javascriptexecutor(CreateTimeSheetButton1, "javascriptExecutorClick",driver);
				}

				driver.findElement(OrangeHRM_TimeSheetEditButton).click();
				Thread.sleep(5000);
				driver.findElement(OrangeHRM_EnterProjectDetails).click();
				Thread.sleep(5000);
				driver.findElement(OrangeHRM_EnterProjectDetails).sendKeys("a");
				Thread.sleep(5000);
				for (int i = 0; i < 2; i++) {

					action.sendKeys(Keys.DOWN);
					action.build().perform();
					Thread.sleep(3000);
				}
				Thread.sleep(3000);
				action.sendKeys(Keys.ENTER).build().perform();
				driver.findElement(OrangeHRM_ActivityDropDownArrow).click();
				Thread.sleep(3000);
				for (int i = 0; i < 2; i++) {

					action.sendKeys(Keys.DOWN);
					action.build().perform();
					Thread.sleep(3000);
				}
				Thread.sleep(3000);
				action.sendKeys(Keys.ENTER).build().perform();
				Thread.sleep(3000);
				for (int i = 0; i < 2; i++) {
					WebElement hours = driver.findElement(By.xpath(
							"(//div[@class='oxd-input-group oxd-input-field-bottom-space'])[" + (i + 3) + "]//input"));
					hours.sendKeys("8");
					Thread.sleep(6000);
				}
				driver.findElement(OrangeHRM_TimesheetSaveButton).click();
				Thread.sleep(3000);

			}

			//ReportExtract.UpdateTestCaseModuleStatus("Pass", CurrentMethod);
		} catch (Exception e) {
			System.out.println(e.getMessage().toString());
			//ReportExtract.UpdateTestCaseModuleStatus("Fail", CurrentMethod);
		}
	}
	
	public static void javascriptexecutor(WebElement element, String type, WebDriver driver) {
		JavascriptExecutor js = (JavascriptExecutor) driver;

		if (type.equalsIgnoreCase("scrollintoview")) {
			js.executeScript("arguments[0].scrollIntoView(true);", element);
		}

		else if (type.equalsIgnoreCase("javascriptExecutorClick")) {
			js.executeScript("arguments[0].click();", element);
		}

	}
	
	public static void scrollby(int Xaxis, int Yaxis,WebDriver driver) {
		JavascriptExecutor scroll = (JavascriptExecutor) driver;
		scroll.executeScript("window.scrollBy(" + Xaxis + "," + Yaxis + ")", "");
		// js.executeScript("arguments[0].scrollIntoView(true);", ViewCartButton);
	}

}
