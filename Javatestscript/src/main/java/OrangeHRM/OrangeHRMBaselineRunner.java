package OrangeHRM;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.changepond.ice.sdk.capture.IceSDK;

import pages.OrangeHRMDashboard;
import pages.OrangeHRMLoginPage;

/**
 * Sets the visual baselines for the OrangeHRM journey. capture() no longer
 * auto-promotes the first screenshot into a baseline -- without a baseline it
 * reports FAIL / NO_BASELINE_SET -- so run this once (and again whenever the UI
 * intentionally changes) before running {@link OrangeHRMTestRunner}.
 *
 * Screen names and the page state at each call must match OrangeHRMTestRunner exactly, or
 * OrangeHRMTestRunner's capture() calls won't find the baseline set here.
 */
public class OrangeHRMBaselineRunner {

	public static void main(String[] args) {
		WebDriver driver = null;
		IceSDK ice = null;

		try {

			driver = new ChromeDriver();
			driver.manage().window().maximize();
			driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");

			new WebDriverWait(driver, Duration.ofSeconds(15))
					.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='username']")));

			ice = IceSDK.init(driver);
			ice.visual.setBaseline(driver, "login_page");

			OrangeHRMLoginPage.Login(driver);
			ice.visual.setBaseline(driver, "dashboard");

			OrangeHRMDashboard.dashboard("admin", driver);
			ice.visual.setBaseline(driver, "clickonadmin");

			OrangeHRMDashboard.ClickonAdmin(driver);
			ice.visual.setBaseline(driver, "edituserpage");
			OrangeHRMDashboard.EditUserPage(driver);

			OrangeHRMLoginPage.Logout(driver);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ice != null) {
				ice.teardown();
			}
			if (driver != null) {
				driver.quit();
			}
		}
	}
}
