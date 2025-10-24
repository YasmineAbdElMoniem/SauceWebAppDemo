package engine;

import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.awt.*;
import java.awt.Rectangle;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * WebDriverBot — A lightweight helper that encapsulates all low-level WebDriver mechanics.
 * Responsibilities:
 * - Initialize and manage browser drivers (Chrome, Firefox, Edge)
 * - Provide consistent waiting, clicking, and typing behavior
 * - Expose small reusable actions with logging and Allure step reporting
 * Business logic should live in Page Objects — not here.
 */
public class WebDriverBot {

    private WebDriver driver;
    private Wait<WebDriver> wait;
    private static final Logger logger = LogManager.getLogger(WebDriverBot.class);
    private final PropertiesReader cfg;

    /**
     * Constructor that initializes WebDriver with configuration settings.
     */
    public WebDriverBot(PropertiesReader cfg) {
        this.cfg = cfg;
        initialize();
    }

    // ============================================================
    // ================ Browser Lifecycle =========================
    // ============================================================

    @Step("Initialize WebDriver with browser: {cfg.browser}")
    private void initialize() {
        switch (cfg.browser.toLowerCase()) {
            case "chrome" -> driver = new ChromeDriver(chromeOptions());
            case "firefox" -> driver = new FirefoxDriver(firefoxOptions());
            case "edge" -> driver = new EdgeDriver(edgeOptions());
            default -> throw new IllegalArgumentException("Unsupported browser: " + cfg.browser);
        }

        this.wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(cfg.pageLoadTimeoutSec))
                .pollingEvery(Duration.ofMillis(300))
                .ignoring(NoSuchElementException.class)
                .ignoring(ElementNotInteractableException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(NotFoundException.class);

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(cfg.pageLoadTimeoutSec));
        applySmartWindowSizing();

        logger.info("✅ Started {} (headless={}, incognito={})", cfg.browser, cfg.headless, cfg.incognito);
    }

    /**
     * Automatically size and position browser window for any device:
     * - Skips explicit sizing in headless mode.
     * - Detects screen bounds and fits window with a small safety margin.
     * - Ensures full visibility on any display (laptop, external, etc.).
     */
    private void applySmartWindowSizing() {
        try {
            if (cfg.headless) {
                logger.info("Headless mode: skipping window sizing (driver controls viewport).");
                return;
            }

            // Detect screen bounds
            Rectangle screen = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getMaximumWindowBounds();

            int screenW = screen.width;
            int screenH = screen.height;

            // Small margins to avoid clipping by OS borders/taskbar
            final int MARGIN_W = 20;
            final int MARGIN_H = 20;

            // Compute usable area dynamically
            int targetW = Math.max(800, screenW - MARGIN_W);
            int targetH = Math.max(600, screenH - MARGIN_H);

            driver.manage().window().setPosition(new Point(0, 0));
            driver.manage().window().setSize(new Dimension(targetW, targetH));

            // Fallback if driver rejects the size
            Dimension applied = driver.manage().window().getSize();
            if (applied.getWidth() < 400 || applied.getHeight() < 300) {
                logger.warn("Window size too small ({}x{}). Maximizing instead.", applied.getWidth(), applied.getHeight());
                driver.manage().window().maximize();
            }

            logger.info("Window positioned at (0,0) with adaptive size {}x{} (screen {}x{})",
                    targetW, targetH, screenW, screenH);

        } catch (Exception e) {
            logger.warn("Window sizing failed ({}). Falling back to maximize.", e.toString());
            try {
                driver.manage().window().maximize();
            } catch (Exception ignored) {
            }
        }
    }

    @Step("Quit browser session")
    public void quit() {
        if (driver != null) {
            driver.quit();
            logger.info("🧹 Browser session closed.");
        }
    }

    // ============================================================
    // ================ Browser Options ===========================
    // ============================================================

    private ChromeOptions chromeOptions() {
        ChromeOptions options = getChromeOptions();
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        var prefs = new java.util.HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        return options;
    }

    private ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--disable-notifications",
                "--disable-popup-blocking",
                "--disable-password-manager-reauthentication",
                "--disable-features=PasswordManagerOnboarding,PasswordLeakDetection,AutofillServerCommunication",
                "--no-first-run",
                "--no-default-browser-check"
        );
        if (cfg.incognito) options.addArguments("--incognito");
        if (cfg.headless) options.addArguments("--headless=new");
        return options;
    }

    private FirefoxOptions firefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        if (cfg.headless) options.addArguments("-headless");
        if (cfg.incognito) options.addPreference("browser.privatebrowsing.autostart", true);
        return options;
    }

    private EdgeOptions edgeOptions() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--disable-notifications", "--disable-popup-blocking",
                "--no-first-run", "--no-default-browser-check");
        if (cfg.incognito) options.addArguments("inprivate");
        if (cfg.headless) options.addArguments("headless=new");
        return options;
    }

    // ============================================================
    // ================ Core Actions (Public) =====================
    // ============================================================

    @Step("Navigate to URL: {url}")
    public void navigateTo(String url) {
        driver.navigate().to(url);
        logger.info("🌐 Navigated to: {}", url);
    }

    @Step("Type '{text}' into element located by {locator}")
    public WebDriverBot type(String text, By locator) {
        wait.until(d -> {
            WebElement el = d.findElement(locator);
            el.clear();
            el.sendKeys(text);
            return true;
        });
        logger.debug("Typed '{}' into {}", text, locator);
        return this;
    }

    @Step("Click element located by {locator}")
    public WebDriverBot click(By locator) {
        wait.until(d -> {
            d.findElement(locator).click();
            return true;
        });
        logger.debug("Clicked element: {}", locator);
        return this;
    }

    @Step("Click element when visible: {locator}")
    public void clickWhenVisible(By locator) {
        waitForVisibility(locator);
        click(locator);
    }

    @Step("Wait for element to be visible: {locator}")
    public void waitForVisibility(By locator) {
        wait.until(d -> d.findElement(locator).isDisplayed());
        logger.debug("Element visible: {}", locator);
    }

    @Step("Wait for element to disappear: {locator}")
    public void waitForGone(By locator) {
        wait.until(d -> d.findElements(locator).isEmpty());
        logger.debug("Element disappeared: {}", locator);
    }

    @Step("Read text from element: {locator}")
    public String getText(By locator) {
        String text = wait.until(d -> d.findElement(locator).getText().trim());
        logger.debug("Read text '{}' from {}", text, locator);
        return text;
    }

    @Step("Read all texts from elements: {locator}")
    public List<String> getTexts(By locator) {
        List<String> texts = new ArrayList<>();
        for (WebElement el : findAll(locator)) {
            texts.add(el.getText().trim());
        }
        logger.debug("Read {} elements' text from {}", texts.size(), locator);
        return texts;
    }

    /**
     * Return the integer text of the FIRST element at locator if present, otherwise defaultValue.
     * Never waits for presence; safe for optional UI like the cart badge.
     */
    @Step("Extract optional integer from {locator} (default={defaultValue})")
    public int getIntIfPresent(By locator, int defaultValue) {
        try {
            var els = findAll(locator);
            if (els.isEmpty()) return defaultValue;
            String txt = els.getFirst().getText().trim();
            String digits = txt.replaceAll("\\D+", "");
            return digits.isEmpty() ? defaultValue : Integer.parseInt(digits);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Step("Check if element is visible: {locator}")
    public boolean isElementVisible(By locator) {
        try {
            boolean visible = driver.findElement(locator).isDisplayed();
            logger.debug("Element visible status ({}): {}", locator, visible);
            return visible;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Step("Check if element is enabled: {locator}")
    public boolean isElementEnabled(By locator) {
        try {
            boolean enabled = driver.findElement(locator).isEnabled();
            logger.debug("Element enabled status ({}): {}", locator, enabled);
            return enabled;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // ============================================================
    // ================ Utilities ================================
    // ============================================================

    public List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }

    public void until(Function<WebDriver, Boolean> condition) {
        wait.until(condition);
    }

    public WebDriver getDriver() {
        return driver;
    }
}