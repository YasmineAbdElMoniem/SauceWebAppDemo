package pages.web;

import engine.WebDriverBot;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

/**
 * Represents the "Checkout Complete" page displayed after a successful purchase.
 * Responsibilities:
 * - Verifies that the success message is visible.
 * - Validates the confirmation text: "Thank you for your order!".
 */
public class CheckoutCompletePage {

    private static final Logger logger = LogManager.getLogger(CheckoutCompletePage.class);
    private final WebDriverBot bot;

    // --- Locators ---
    private final By completeHeader = By.cssSelector(".complete-header");

    public CheckoutCompletePage(WebDriverBot bot) {
        this.bot = bot;
    }

    /**
     * Validates that the order completion message appears and matches the expected text.
     *
     * @return true if the confirmation header is visible and matches expected text, false otherwise.
     */
    @Step("Verify order completion message is displayed correctly")
    public boolean isOrderComplete() {
        try {
            // Ensure header is visible
            if (!bot.isElementVisible(completeHeader)) {
                logger.warn("❌ Order completion header not visible");
                return false;
            }

            // Validate the message text
            String actualText = bot.getText(completeHeader).trim();
            final String expectedText = "Thank you for your order!";

            if (actualText.equalsIgnoreCase(expectedText)) {
                logger.info("✅ Order completed successfully with confirmation text: '{}'", actualText);
                return true;
            } else {
                logger.warn("⚠️ Header visible but text mismatch. Found '{}', expected '{}'", actualText, expectedText);
                return false;
            }

        } catch (Exception e) {
            logger.error("❌ Failed to verify order completion text: {}", e.getMessage());
            return false;
        }
    }
}