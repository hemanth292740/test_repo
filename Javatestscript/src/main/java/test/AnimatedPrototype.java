package test;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.changepond.ice.sdk.capture.IceSDK;

/**
 * Responsive-testing journey (J-001): Hotel login page vs its Figma-generated .gspec.
 *
 * Run from ICE (Execute Tests -> Select Manually) with the Responsive device sizes ticked.
 * Needs responsive-specs/RESP-001/login_page.gspec in the tenant's Git repo (Automate ->
 * Sync from Figma) and a running galen-worker (ice-sidecar-workers).
 *
 * How the check reaches galen-worker: ice.responsiveness.check() posts type "responsiveness" with
 * the page URL to /api/v1/analyse. The backend reads the run's ticked sizes and the journey's .gspec from Git and
 * queues the check on the ice:stream:responsive Redis stream; galen-worker loads the page in its own
 * headless Chromium at each size, checks it against the .gspec and posts the verdicts back
 * (ice.visual_diff_results, one row per size).
 */
public class AnimatedPrototype {

	private static final String PAGE_URL = "http://172.17.13.12:3007";

	/**
	 * Screen name the check is reported under. Must be the Figma frame name the journey was synced
	 * with: Design Sync stores the spec as responsive-specs/{journeyId}/login_page.gspec, and
	 * galen-worker finds it by this name.
	 */


	public static void main(String[] args) {
		WebDriver driver = null;
		IceSDK ice = null;

		try {
			driver = new ChromeDriver();
			driver.manage().window().maximize();

			ice = IceSDK.init(driver);

			driver.get(PAGE_URL);


			// Show the page at the sizes ticked on Execute Tests -> Responsive (widest first). Only this
			// window changes; the real check runs in galen-worker's own browser.
			// Chrome will not go below its minimum window width, so 360 shows as about 500 here.
			// Outside an ICE run nothing is ticked, so fall back to a fixed set.
			List<String> sizes = ice.responsiveness.viewports();

			for (String size : sizes) {
				ice.responsiveness.setViewport(driver, size);
				// long enough to see the layout at this size
			}

			// Hand the page to ICE; galen-worker checks it at every ticked size.
			ice.responsiveness.check(driver, "Log-In");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ice != null) {
				ice.teardown(); // waits for the analyse POST (and other uploads) to finish
			}
			if (driver != null) {
				driver.quit();
			}
		}
	}

	/** Waits until the login form is rendered, so the check runs against the real page, not a blank one. */

}
