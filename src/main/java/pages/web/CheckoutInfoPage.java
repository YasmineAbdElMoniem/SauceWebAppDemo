package pages.web;

import engine.WebDriverBot;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

/**
 * Represents Step 1 of the Checkout flow ("Your Information").
 * This page is responsible for filling out user details such as
 * first name, last name, and postal code before proceeding to the
 * Overview page or returning to the Cart.
 */
public class CheckoutInfoPage {

    private static final Logger logger = LogManager.getLogger(CheckoutInfoPage.class);
    private final WebDriverBot bot;

    // ---------- Locators ----------
    private final By firstNameField = By.id("first-name");
    private final By lastNameField = By.id("last-name");
    private final By postalField = By.id("postal-code");
    private final By continueBtn = By.id("continue");

    // ---------- Constructor ----------
    public CheckoutInfoPage(WebDriverBot bot) {
        this.bot = bot;
        logger.info("✅ CheckoutInfoPage initialized successfully");
    }

    // ============================================================
    // ==================== Form Actions ==========================
    // ============================================================

    /**
     * Fills in the Checkout Information form with the provided details.
     *
     * @param first First name of the customer.
     * @param last  Last name of the customer.
     * @param zip   Postal or ZIP code.
     */
    @Step("Fill checkout form with: First='{first}', Last='{last}', ZIP='{zip}'")
    public void fillInfo(String first, String last, String zip) {
        String f = first == null ? "" : first.trim();
        String l = last == null ? "" : last.trim();
        String z = zip == null ? "" : zip.trim();

        logger.info("Filling Checkout form → First='{}', Last='{}', ZIP='{}'", f, l, z);

        bot.type(f, firstNameField)
                .type(l, lastNameField)
                .type(z, postalField);

    }

    /**
     * Fills checkout info and continues to the Overview page.
     * Waits for form fields before typing for improved stability.
     *
     * @param first First name
     * @param last  Last name
     * @param zip   Postal code
     * @return CheckoutOverviewPage instance
     */
    @Step("Fill checkout form with First='{first}', Last='{last}', ZIP='{zip}' and continue to Overview")
    public CheckoutOverviewPage fillAndContinue(String first, String last, String zip) {
        bot.waitForVisibility(firstNameField);
        bot.waitForVisibility(lastNameField);
        bot.waitForVisibility(postalField);
        bot.waitForVisibility(continueBtn);

        logger.info("Filling form and continuing to Overview page");
        fillInfo(first, last, zip);
        return continueToOverview();
    }

    /**
     * Clicks the Continue button to proceed to the Overview page.
     *
     * @return CheckoutOverviewPage instance.
     */
    @Step("Click Continue to proceed to Overview page")
    public CheckoutOverviewPage continueToOverview() {
        bot.click(continueBtn);
        logger.info("Navigated to Checkout Overview page");
        return new CheckoutOverviewPage(bot);
    }
}