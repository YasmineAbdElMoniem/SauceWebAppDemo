package pages.web;

import engine.WebDriverBot;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

/**
 * Represents the Sauce Demo Login page.
 * Responsibilities:
 * - Navigates to the login URL.
 * - Enters username and password.
 * - Submits the form to reach the Products page.
 * - Verifies if the user is currently on the login screen.
 */
public class LoginPage {

    private static final Logger logger = LogManager.getLogger(LoginPage.class);
    private final WebDriverBot bot;

    // --- Locators ---
    private final By usernameField = By.id("user-name");
    private final By passwordField = By.id("password");
    private final By loginButton = By.id("login-button");

    public LoginPage(WebDriverBot bot) {
        this.bot = bot;
        logger.info("LoginPage initialized");
    }

    /**
     * Opens the Sauce Demo login page.
     *
     * @param url the base URL from configuration (Ex: <a href="https://www.saucedemo.com/">...</a>)
     * @return this LoginPage instance for chaining
     */
    @Step("Navigate to Sauce Demo login page: {url}")
    public LoginPage navigateTo(String url) {
        bot.navigateTo(url);
        logger.info("Navigated to login page: {}", url);
        return this;
    }

    /**
     * Logs in using the provided credentials.
     *
     * @param username SauceDemo username
     * @param password SauceDemo password
     * @return ProductsPage instance after successful login
     */
    @Step("Login with username: {username} and password: [PROTECTED]")
    public ProductsPage login(String username, String password) {
        bot.type(username, usernameField)
                .type(password, passwordField)
                .click(loginButton);
        logger.info("Login attempt with username '{}'", username);
        return new ProductsPage(bot);
    }

    /**
     * Verifies if the user is still on the login page (by checking login button visibility).
     *
     * @return true if login button is visible; false otherwise
     */
    @Step("Check if user is on login page (verifies login button visibility)")
    public boolean isAtLoginPage() {
        boolean visible = bot.isElementVisible(loginButton);
        logger.info("Login page visible: {}", visible);
        return visible;
    }
}