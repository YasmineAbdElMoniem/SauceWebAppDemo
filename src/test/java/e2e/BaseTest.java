package e2e;

import engine.PropertiesReader;
import engine.WebDriverBot;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * <b>BaseTest</b> — foundational class for all end-to-end (E2E) test cases.
 * <p>
 * Provides reusable setup, teardown, and Allure screenshot logic for UI tests.
 * </p>
 *
 * Responsibilities:
 * <ul>
 *   <li>Load configuration from <code>config.properties</code>.</li>
 *   <li>Initialize {@link WebDriverBot} before running tests.</li>
 *   <li>Capture screenshots automatically after each test (pass/fail).</li>
 *   <li>Quit the browser session after all tests complete.</li>
 * </ul>
 */
public abstract class BaseTest {

    protected WebDriverBot bot;
    protected PropertiesReader cfg;

    private static final Logger logger = LogManager.getLogger(BaseTest.class);

    // ------------------------------------------------------------------------
    // One-time Setup and Teardown
    // ------------------------------------------------------------------------

    /**
     * Loads configuration and initializes WebDriver before any test class executes.
     */
    @BeforeClass(alwaysRun = true)
    @Step("Initialize WebDriver and load configuration before executing tests")
    public void setup() throws Exception {
        logger.info("🔧 Starting BaseTest setup...");
        cfg = new PropertiesReader();
        logger.info("✅ Configuration loaded successfully");

        bot = new WebDriverBot(cfg);
        logger.info("✅ WebDriverBot initialized (Browser: {}, Headless: {}, Incognito: {})",
                cfg.browser, cfg.headless, cfg.incognito);
    }

    /**
     * Cleans up WebDriver resources after all tests finish.
     */
    @AfterClass(alwaysRun = true)
    @Step("Clean up: Quit WebDriver after all tests are finished")
    public void teardown() {
        logger.info("🧹 Starting test teardown...");
        if (bot != null) {
            bot.quit();
            logger.info("✅ Browser session terminated successfully");
        } else {
            logger.warn("⚠️ WebDriverBot instance was null, nothing to quit");
        }
    }

    // ------------------------------------------------------------------------
    // Per-Test Hooks
    // ------------------------------------------------------------------------

    /**
     * Logs the start of each test method for improved readability in logs and reports.
     *
     * @param result The TestNG {@link ITestResult} containing method details.
     */
    @BeforeMethod(alwaysRun = true)
    public void beforeEachTest(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.info("🚀 Starting test: {}", testName);
    }

    /**
     * Captures a screenshot after each test (PASS, FAIL, or SKIP)
     * and attaches it to the Allure report.
     *
     * @param result The TestNG {@link ITestResult} representing test outcome.
     */
    @AfterMethod(alwaysRun = true)
    public void attachScreenshotToAllure(ITestResult result) {
        String name = "%s — %s".formatted(
                result.getMethod().getMethodName(),
                result.getStatus() == ITestResult.SUCCESS ? "PASS" :
                        result.getStatus() == ITestResult.FAILURE ? "FAIL" : "SKIP"
        );

        // Trigger screenshot capture for Allure
        byte[] ignored = takeScreenshot(name);

        logger.info("📎 Screenshot attached for test result: {}", name);
    }

    // ------------------------------------------------------------------------
    // Allure Attachment Helper
    // ------------------------------------------------------------------------

    /**
     * Takes a screenshot of the current browser window and attaches it to Allure.
     *
     * @param name Descriptive name for the attachment (test name + status).
     * @return Screenshot as a PNG byte array, or empty byte array if capture fails.
     */
    @Attachment(value = "{name}", type = "image/png")
    @SuppressWarnings("unused") // Used reflectively by Allure
    protected byte[] takeScreenshot(String name) {
        try {
            logger.info("📸 Capturing screenshot for test: {}", name);
            return ((TakesScreenshot) bot.getDriver()).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to capture screenshot: {}", e.getMessage());
            return new byte[0];
        }
    }
}