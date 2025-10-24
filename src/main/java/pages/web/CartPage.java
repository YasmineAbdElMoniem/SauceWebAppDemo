package pages.web;

import engine.WebDriverBot;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

/**
 * Represents the Cart page in SauceDemo.
 * Provides reusable actions and checkpoints for cart operations such as:
 * verifying items, removing items, continuing shopping, and starting checkout.
 */
public class CartPage {

    private static final Logger logger = LogManager.getLogger(CartPage.class);
    private final WebDriverBot bot;

    // ---------- UI Locators ----------
    private final By cartBadge = By.className("shopping_cart_badge");
    private final By checkoutBtn = By.id("checkout");
    private final By continueBtn = By.id("continue-shopping");

    public CartPage(WebDriverBot bot) {
        this.bot = bot;
        logger.info("✅ CartPage initialized successfully");
    }

    // ---------- Row locators ----------

    /**
     * Row that contains an item by its name.
     */
    private By itemRowByName(String productName) {
        String xpath = String.format(
                "//div[@class='cart_item'][.//div[normalize-space(text())='%s']]",
                productName
        );
        logger.debug("Building dynamic locator for item row → {}", xpath);
        return By.xpath(xpath);
    }

    /**
     * The 'Remove' button but scoped to the specific row
     */
    private By removeButtonFor(String productName) {
        String xpath = String.format(
                "//div[@class='cart_item'][.//div[normalize-space(text())='%s']]//button[contains(@id,'remove-')]",
                productName
        );
        logger.debug("Building dynamic locator for Remove button → {}", xpath);
        return By.xpath(xpath);
    }
    // ============================================================
    // =============== Badge (Cart Count) Helpers =================
    // ============================================================

    /**
     * @return the number shown on the cart badge, or 0 if badge not present.
     */
    @Step("Get current cart badge count")
    public int getCartCount() {
        return bot.getIntIfPresent(cartBadge, 0);
    }

    /**
     * Wait until the cart badge equals the expected value.
     */
    @Step("Wait until cart badge count equals {expected}")
    private void waitForCartCount(int expected) {
        bot.until(d -> getCartCount() == expected);
        logger.debug("Waited for cart count to equal {}", expected);
    }

    /**
     * Wait until the item row disappears from the DOM.
     */
    @Step("Wait until product '{productName}' row disappears from cart")
    private void waitRowDisappears(String productName) {
        bot.waitForGone(itemRowByName(productName));
        logger.debug("Row for '{}' disappeared from cart", productName);
    }

    // ============================================================
    // =============== Public Checks / Actions ====================
    // ============================================================

    /**
     * Verifies that all specified products exist in the cart.
     *
     * @param productNames Product names to verify.
     * @return true if all exist, false otherwise.
     */
    @Step("Verify products are present in the cart: {productNames}")
    public boolean areProductsInCart(String... productNames) {
        for (String name : productNames) {
            boolean exists = bot.exists(itemRowByName(name)); // faster/clearer probe
            if (!exists) {
                logger.warn("❌ Product '{}' NOT found in cart", name);
                return false;
            }
            logger.info("✅ Product '{}' is present in the cart", name);
        }
        return true;
    }

    /**
     * Removes a specific product from the cart
     *
     * @param productName Product name to remove.
     * @return true if we initiated removal (row existed); false if not found.
     */
    @Step("Remove product '{productName}' from cart")
    public boolean removeProduct(String productName) {
        if (!bot.exists(itemRowByName(productName))) {
            logger.warn("⚠️ Product '{}' not found in cart — cannot remove", productName);
            return false;
        }
        bot.click(removeButtonFor(productName));
        waitRowDisappears(productName);
        logger.info("🗑️ Product '{}' removed from cart", productName);
        return true;
    }

    /**
     * Removes a product and validates the cart badge decremented correctly.
     *
     * @param productName Product name.
     * @return true if cart count decreased by 1 and row disappeared.
     */
    @Step("Remove product '{productName}' and verify badge decrements by 1")
    public boolean removeProductAndVerifyBadge(String productName) {
        int before = getCartCount();
        int expected = Math.max(0, before - 1);

        if (!removeProduct(productName)) return false;

        waitForCartCount(expected);
        int after = getCartCount();
        boolean ok = (after == expected);

        if (ok) {
            logger.info("✅ Cart badge decremented correctly after removing '{}': {} → {}", productName, before, after);
        } else {
            logger.warn("⚠️ Cart badge mismatch after removing '{}': expected {}, found {}", productName, expected, after);
        }
        return ok;
    }

    // ============================================================
    // ===================== Navigation ==========================
    // ============================================================

    /**
     * Clicks “Continue Shopping” and returns to Products page.
     */
    @Step("Continue shopping (navigate back to Products page)")
    public ProductsPage continueShopping() {
        bot.click(continueBtn);
        logger.info("Navigated back to Products page from Cart");
        return new ProductsPage(bot);
    }

    /**
     * Clicks “Checkout” and moves to Checkout Info page.
     */
    @Step("Proceed to Checkout from Cart page")
    public CheckoutInfoPage startCheckout() {
        bot.click(checkoutBtn);
        logger.info("Navigated to Checkout Information page");
        return new CheckoutInfoPage(bot);
    }

    /**
     * Quick probe (no wait) whether Checkout is enabled right now.
     */
    @Step("Check if Checkout button is enabled")
    public boolean isCheckoutButtonEnabled() {
        boolean enabled = bot.isElementEnabled(checkoutBtn);
        logger.info("Checkout button enabled: {}", enabled);
        return enabled;
    }
}