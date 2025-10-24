package pages.web;

import engine.WebDriverBot;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductsPage {

    private static final Logger logger = LogManager.getLogger(ProductsPage.class);
    private final WebDriverBot bot;

    // ---------- Static Locators ----------
    private final By titleText = By.xpath("//span[@class='title']");
    private final By pricesPath = By.xpath("//div[@class='inventory_item_price']");
    private final By sortSelect = By.xpath("//select[@class='product_sort_container']");
    private final By lowToHigh = By.xpath("//option[@value='lohi']");
    private final By cartBadge = By.className("shopping_cart_badge");
    private final By cartIcon = By.className("shopping_cart_link");
    private final By menuBtn = By.id("react-burger-menu-btn");
    private final By logoutLink = By.id("logout_sidebar_link");

    public ProductsPage(WebDriverBot bot) {
        this.bot = bot;
        logger.info("ProductsPage initialized");
    }

    // ---------- name/locator helpers ----------
    private String slug(String name) {
        return name.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-");
    }

    private By addButton(String name) {
        return By.xpath("//button[@data-test='add-to-cart-" + slug(name) + "']");
    }

    private By removeButton(String name) {
        return By.xpath("//button[@data-test='remove-" + slug(name) + "']");
    }

    // ---------- header ----------
    @Step("Get header text on products page")
    public String getPageTitle() {
        String title = bot.getText(titleText);
        logger.info("Page Title: {}", title);
        return title;
    }

    // ---------- sorting ----------
    @Step("Apply sort: Price Low → High")
    public ProductsPage chooseSortLowToHigh() {
        // Bot.click has its own single wait; we make sure the select is present before selecting the option.
        bot.click(sortSelect).click(lowToHigh);
        logger.info("Applied sort: Low → High");
        return this;
    }

    @Step("Read all product prices from listing")
    public List<Double> getPrices() {
        List<String> raw = bot.getTexts(pricesPath);
        List<Double> out = new ArrayList<>();
        for (String t : raw) {
            String cleaned = t.replaceAll("[^\\d.]", "");
            if (!cleaned.isEmpty()) {
                try {
                    out.add(Double.parseDouble(cleaned));
                } catch (NumberFormatException ignored) {
                    // ignore malformed price entries; keep going
                }
            }
        }
        logger.debug("Read prices: {}", out);
        return out;
    }

    @Step("Verify prices are sorted Low → High")
    public boolean isSortedLowToHigh(List<Double> actual) {
        List<Double> expected = new ArrayList<>(actual);
        Collections.sort(expected);
        boolean ok = actual.equals(expected);
        if (!ok) logger.warn("Prices not sorted: actual={} expected={}", actual, expected);
        return ok;
    }

    @Step("Sort products Low → High and verify")
    public boolean sortProductPriceFromLowToHigh() {
        return chooseSortLowToHigh().isSortedLowToHigh(getPrices());
    }

    // ---------- cart badge ----------

    /**
     * @return the number shown on the cart badge, or 0 if badge not present.
     */
    @Step("Get current cart badge count")
    public int getCartCount() {
        return bot.getIntIfPresent(cartBadge, 0);
    }

    @Step("Wait until cart count equals {expected}")
    private void waitForCartCount(int expected) {
        bot.until(d -> getCartCount() == expected);
        logger.info("Cart count reached {}", expected);
    }

    // ---------- landing actions ----------
    @Step("Check if '{productName}' is already in cart (Remove button present)")
    public boolean isInCartOnLanding(String productName) {
        boolean inCart = bot.exists(removeButton(productName)); // quick probe; visibility not required
        logger.info("'{}' in cart on landing? {}", productName, inCart);
        return inCart;
    }

    @Step("Click 'Add to cart' for '{productName}' on landing")
    public ProductsPage clickAddOnLanding(String productName) {
        bot.click(addButton(productName));
        return this;
    }

    @Step("Click 'Remove' for '{productName}' on landing")
    public ProductsPage clickRemoveOnLanding(String productName) {
        bot.click(removeButton(productName));
        return this;
    }

    @Step("Wait until '{productName}' button swaps to 'Remove'")
    public ProductsPage waitSwappedToRemove(String productName) {
        bot.waitForVisibility(removeButton(productName));
        return this;
    }

    @Step("Wait until '{productName}' button swaps back to 'Add to cart'")
    public ProductsPage waitSwappedToAdd(String productName) {
        bot.waitForVisibility(addButton(productName));
        return this;
    }

    // ---------- public flows ----------
    @Step("Add '{productName}' to cart (idempotent; verify button swap and badge +1)")
    public ProductsPage addProduct(String productName) {
        try {
            if (isInCartOnLanding(productName)) return this;
            int before = getCartCount();
            clickAddOnLanding(productName)
                    .waitSwappedToRemove(productName)
                    .waitForCartCount(before + 1);
            logger.info("Added '{}' ({} → {})", productName, before, before + 1);
        } catch (TimeoutException | NoSuchElementException e) {
            logger.error("Add failed for '{}': {}", productName, e.getMessage());
        }
        return this;
    }

    @Step("Add products {productNames} and verify cart count increases accordingly")
    public boolean addProductsAndVerifyCart(String... productNames) {
        if (productNames == null || productNames.length == 0) {
            logger.warn("No product names supplied to addProductsAndVerifyCart()");
            return true; // nothing to do
        }
        int start = getCartCount();
        int toAdd = 0;
        for (String name : productNames) {
            if (name == null || name.isBlank()) {
                logger.warn("Skipped blank/null product name in addProductsAndVerifyCart()");
                continue;
            }
            if (isInCartOnLanding(name)) {
                logger.info("'{}' already in cart — skipping add", name);
                continue;
            }
            toAdd++;
            addProduct(name); // this already waits for button swap + badge +1 for this item
        }
        int expected = start + toAdd;
        // Final confirmation that the badge reached the intended total
        waitForCartCount(expected);
        int end = getCartCount();
        boolean ok = (end == expected);
        if (ok) {
            logger.info("✅ Added {} new item(s) successfully: {} → {}", toAdd, start, end);
        } else {
            logger.warn("❌ Cart count mismatch after adding items: expected {}, got {} (start was {})",
                    expected, end, start);
        }
        return ok;
    }

    @Step("Remove '{productName}' from landing (idempotent; verify button swap and badge -1)")
    public boolean removeProductFromLanding(String productName) {
        try {
            if (!isInCartOnLanding(productName)) return false;
            int before = getCartCount();
            int expected = Math.max(0, before - 1);
            clickRemoveOnLanding(productName)
                    .waitSwappedToAdd(productName)
                    .waitForCartCount(expected);
            logger.info("Removed '{}' ({} → {})", productName, before, expected);
            return true;
        } catch (TimeoutException | NoSuchElementException e) {
            logger.error("Remove failed for '{}': {}", productName, e.getMessage());
            return false;
        }
    }

    // ---------- navigation ----------
    @Step("Open the cart page")
    public CartPage openCart() {
        bot.click(cartIcon);
        return new CartPage(bot);
    }

    @Step("Logout via menu")
    public LoginPage logout() {
        bot.click(menuBtn).click(logoutLink); // each click is a single waited action
        logger.info("Logged out via menu and redirected to login page");
        return new LoginPage(bot);
    }
}