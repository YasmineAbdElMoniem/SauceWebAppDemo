package pages.web;

import engine.WebDriverBot;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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
    private final By removeBtnInRow = By.xpath(".//button[contains(@id,'remove-')]");
    private final By checkoutBtn = By.id("checkout");
    private final By continueBtn = By.id("continue-shopping");

    // ---------- Constructor ----------
    public CartPage(WebDriverBot bot) {
        this.bot = bot;
        logger.info("✅ CartPage initialized successfully");
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
     * Waits until the cart badge count matches the expected number.
     *
     * @param expected Expected badge value.
     */
    @Step("Wait until cart badge count equals {expected}")
    private void waitForCartCount(int expected) {
        bot.until(d -> getCartCount() == expected);
        logger.debug("Waited for cart count to equal {}", expected);
    }

    // ============================================================
    // =============== Row (Cart Item) Helpers ====================
    // ============================================================

    /**
     * Returns the locator of a specific cart item row by product name.
     */
    private By itemRowByName(String productName) {
        return By.xpath("//div[@class='cart_item' and .//div[@class='inventory_item_name' " +
                "and normalize-space()='" + productName + "']]");
    }

    /**
     * Finds and returns the WebElement row for a given product.
     *
     * @param productName The product name as displayed in the cart.
     * @return The row element, or {@code null} if not found.
     */
    private WebElement findRow(String productName) {
        var rows = bot.findAll(itemRowByName(productName));
        return rows.isEmpty() ? null : rows.getFirst();
    }

    /**
     * Clicks the “Remove” button within the given row.
     *
     * @param row Row element containing the target product.
     */
    private void clickRemoveInRow(WebElement row) {
        row.findElement(removeBtnInRow).click();
        logger.debug("Clicked Remove button inside row");
    }

    /**
     * Waits until the row for a given product disappears from the DOM.
     *
     * @param productName The product name.
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
            boolean exists = !bot.findAll(itemRowByName(name)).isEmpty();
            if (!exists) {
                logger.warn("❌ Product '{}' NOT found in cart", name);
                return false;
            }
            logger.info("✅ Product '{}' is present in the cart", name);
        }
        return true;
    }

    /**
     * Removes a specific product from the cart.
     *
     * @param productName Product name to remove.
     * @return true if removed, false if not found.
     */
    @Step("Remove product '{productName}' from cart")
    public boolean removeProduct(String productName) {
        WebElement row = findRow(productName);
        if (row == null) {
            logger.warn("⚠️ Product '{}' not found in cart — cannot remove", productName);
            return false;
        }
        clickRemoveInRow(row);
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

        if (!removeProduct(productName)) {
            return false;
        }

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
     *
     * @return ProductsPage instance.
     */
    @Step("Continue shopping (navigate back to Products page)")
    public ProductsPage continueShopping() {
        bot.click(continueBtn);
        logger.info("Navigated back to Products page from Cart");
        return new ProductsPage(bot);
    }

    /**
     * Clicks “Checkout” and moves to Checkout Info page.
     *
     * @return CheckoutInfoPage instance.
     */
    @Step("Proceed to Checkout from Cart page")
    public CheckoutInfoPage startCheckout() {
        bot.click(checkoutBtn);
        logger.info("Navigated to Checkout Information page");
        return new CheckoutInfoPage(bot);
    }

    /**
     * Checks if the checkout button is currently enabled.
     *
     * @return true if enabled, false otherwise.
     */
    @Step("Check if Checkout button is enabled")
    public boolean isCheckoutButtonEnabled() {
        boolean enabled = bot.isElementEnabled(checkoutBtn);
        logger.info("Checkout button enabled: {}", enabled);
        return enabled;
    }
}