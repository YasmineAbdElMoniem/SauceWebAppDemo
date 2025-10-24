package pages.web;

import engine.WebDriverBot;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

/**
 * Represents the "Checkout: Overview" page (Step 2 in the checkout flow).
 * Responsibilities:
 * - Displays summary of selected products.
 * - Allows finishing or canceling the checkout process.
 * - Provides count of overview items.
 */
public class CheckoutOverviewPage {

    private static final Logger logger = LogManager.getLogger(CheckoutOverviewPage.class);
    private final WebDriverBot bot;

    // --- Locators ---
    private final By finishBtn = By.id("finish");
    private final By overviewItems = By.cssSelector(".cart_item");
    private final By cancelBtn = By.id("cancel");

    public CheckoutOverviewPage(WebDriverBot bot) {
        this.bot = bot;
        logger.info("CheckoutOverviewPage initialized");
    }

    /**
     * Counts the number of products listed in the checkout overview.
     *
     * @return total number of items in the overview list
     */
    @Step("Count items listed on the checkout overview page")
    public int overviewItemCount() {
        int count = bot.findAll(overviewItems).size();
        logger.info("Overview contains {} item(s)", count);
        return count;
    }

    /**
     * Cancels the checkout process from the overview page and navigates back to Products.
     *
     * @return ProductsPage instance after cancellation
     */
    @Step("Cancel checkout from overview (return to products page)")
    public ProductsPage cancelCheckoutOverview() {
        bot.click(cancelBtn);
        logger.info("Checkout canceled from overview page, returning to Products page");
        return new ProductsPage(bot);
    }

    /**
     * Finishes the checkout process and navigates to the Checkout Complete page.
     *
     * @return CheckoutCompletePage instance after finishing order
     */
    @Step("Finish order from overview page")
    public CheckoutCompletePage finishOrder() {
        bot.click(finishBtn);
        logger.info("Finish button clicked, navigating to Checkout Complete page");
        return new CheckoutCompletePage(bot);
    }
}
