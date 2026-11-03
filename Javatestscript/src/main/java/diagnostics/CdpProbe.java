package diagnostics;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.Event;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.json.JsonInput;

/**
 * Standalone check that Selenium's CDP bindings actually match the installed Chrome.
 *
 * <p>Deliberately does not touch the ICE SDK or the backend: it reproduces exactly the
 * three calls {@code IceSDK.IceNetwork.start()} makes -- {@code getDevTools()},
 * {@code createSession()}, {@code send(Network.enable)} -- and then subscribes to the
 * same three lifecycle events. If this prints non-zero counts, the CDP layer is sound
 * and any later "0 records" is an ICE-side problem, not an environment one.
 *
 * <p>The failure this exists to catch is silent: Selenium's {@code CdpVersionFinder}
 * hands back a no-op CDP implementation when no bundled {@code vNNN} binding is within
 * its tolerance of the browser's major version, and that only surfaces on the first
 * {@code send()}.
 *
 * <p>Run with: {@code mvn -q compile exec:java -Dexec.mainClass=diagnostics.CdpProbe}
 */
public class CdpProbe {

    private static final String URL =
            "https://opensource-demo.orangehrmlive.com/web/index.php/auth/login";

    public static void main(String[] args) throws Exception {
        System.out.println("[probe] Selenium on classpath : "
                + new org.openqa.selenium.BuildInfo().getReleaseLabel());

        WebDriver driver = new ChromeDriver();
        AtomicInteger sent = new AtomicInteger();
        AtomicInteger received = new AtomicInteger();
        AtomicInteger finished = new AtomicInteger();

        try {
            System.out.println("[probe] Chrome reported     : "
                    + ((org.openqa.selenium.HasCapabilities) driver).getCapabilities()
                            .getCapability("browserVersion"));

            DevTools dt = ((HasDevTools) driver).getDevTools();
            dt.createSession();
            // First send() is where a no-op CDP implementation throws.
            dt.send(new Command<Void>("Network.enable", Map.of()));
            System.out.println("[probe] Network.enable      : OK (real CDP session)");

            dt.addListener(raw("Network.requestWillBeSent"), e -> sent.incrementAndGet());
            dt.addListener(raw("Network.responseReceived"), e -> received.incrementAndGet());
            dt.addListener(raw("Network.loadingFinished"), e -> finished.incrementAndGet());

            driver.get(URL);
            Thread.sleep(Duration.ofSeconds(8).toMillis());

            System.out.println("[probe] requestWillBeSent   : " + sent.get());
            System.out.println("[probe] responseReceived    : " + received.get());
            System.out.println("[probe] loadingFinished     : " + finished.get());
            System.out.println("[probe] VERDICT             : "
                    + (sent.get() > 0 && received.get() > 0 && finished.get() > 0
                            ? "PASS - live CDP events captured"
                            : "FAIL - no live CDP events"));
        } finally {
            driver.quit();
        }
    }

    /**
     * Version-independent event binding, the same escape hatch the SDK uses instead of
     * importing a generated {@code org.openqa.selenium.devtools.vNNN} class.
     */
    private static Event<Object> raw(String method) {
        return new Event<>(method, (JsonInput input) -> input.read(Map.class));
    }
}
