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

public class OrangeHRMTestRunner {

	public static void main(String[] args) {
		WebDriver driver = null;
		IceSDK ice = null;

		try {
			driver = new ChromeDriver();
			driver.manage().window().maximize();

			// Init + start network capture BEFORE the first navigation -- attached after
			// driver.get(), the login page's own requests are missed or recorded incomplete.
			ice = IceSDK.init(driver);
			boolean started = ice.network.start(driver);
			System.out.println("[ICE] network.start() = " + started
					+ ", lastStartError = " + ice.network.lastStartError());

			// Label first, then navigate -- so the login page's requests are "Login", not "default".
			ice.network.capture("Login");
			driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");

			new WebDriverWait(driver, Duration.ofSeconds(15))
					.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='username']")));

			ice.accessibility.analyse(driver, "Login");
			ice.visual.capture(driver, "login_page");
			// PerformanceObserver is a page-level JS API (window.__iceEntries), not a CDP
			// listener -- it does NOT survive a navigation like ice.network.start()'s CDP
			// Network domain listener does. Must be (re-)armed on this exact page, right
			// before capturing it, or LCP/CLS/FID are silently empty (only ttfb_ms/page_load_ms,
			// from performance.timing, still work -- confirmed live, see FR-056 session notes).
			ice.performance.start(driver);
			ice.performance.capture(driver, "Login");
			ice.security.passiveScan(driver, "Login");

			OrangeHRMLoginPage.Login(driver);

			ice.network.capture("Dashboard");
			ice.accessibility.analyse(driver, "Dashboard");
			ice.visual.capture(driver, "dashboard");
			// Re-arm per page -- see the Login comment above.
			ice.performance.start(driver);
			ice.performance.capture(driver, "Dashboard");
			ice.security.passiveScan(driver, "Dashboard");
			// Active scan queues a real OWASP ZAP crawl/attack on the backend -- expensive
			// compared to passive, so keep real calls to a couple of screens, not every one.
			// 2-arg overload here: activeScan() captures driver.getCurrentUrl() itself.
			ice.security.activeScan(driver, "Dashboard");

			OrangeHRMDashboard.dashboard("admin", driver);

			ice.network.capture("Admin");
			ice.accessibility.analyse(driver, "ClickonAdmin");
			ice.visual.capture(driver, "clickonadmin");
			// Re-arm per page -- see the Login comment above.
			ice.performance.start(driver);
			ice.performance.capture(driver, "ClickonAdmin");
			ice.security.passiveScan(driver, "ClickonAdmin");

			OrangeHRMDashboard.ClickonAdmin(driver);

			ice.network.capture("EditUserPage");
			ice.accessibility.analyse(driver, "EditUserPage");
			ice.visual.capture(driver, "edituserpage");
			// Re-arm per page -- see the Login comment above.
			ice.performance.start(driver);
			ice.performance.capture(driver, "EditUserPage");
			ice.security.passiveScan(driver, "EditUserPage");
			// 3-arg overload: explicit pageUrl instead of letting activeScan() capture
			// driver.getCurrentUrl() itself -- must be an absolute http(s) URL (ICE SDK Engine
			// Calls spec row 6). Still only one active scan per run (see Dashboard above).
			ice.security.activeScan(driver, "EditUserPage", driver.getCurrentUrl());

			OrangeHRMDashboard.EditUserPage(driver);

			OrangeHRMLoginPage.Logout(driver);

			// Drains any in-flight performance POSTs. Must run before network.stop()/teardown()
			// so the drain isn't racing the final flush below.
			ice.performance.stop(driver);

			// REQUIRED: computes timings and sends every network record in one POST.
			// Without it nothing captured above is ever sent.
			int flushed = ice.network.stop();
			System.out.println("[ICE] network.stop() flushed " + flushed + " record(s)");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// FR-048 Performance Load: fire the journey's load test here, in finally, so it
			// still runs even if a step above threw. "" = whole-journey plan (no per-screen
			// sub-plan). Fire-and-forget: JMeter runs server-side; status/results land on the
			// Execute screen and in Analytics. One load test per plan per run -- a later call
			// for the same plan is skipped (204), so calling it once here is correct.
			if (ice != null) {
				ice.loadtest.run(driver, "");
				// waits for the network POST and every passiveScan/activeScan security/performance POST to finish
				ice.teardown();
			}
			if (driver != null) {
				driver.quit();
			}
		}
	}
}
