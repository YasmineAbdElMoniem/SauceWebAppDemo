package e2e;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.web.CartPage;
import pages.web.CheckoutOverviewPage;
import pages.web.LoginPage;
import pages.web.ProductsPage;

/**
 * Scenario S1 — Full end-to-end “Happy Purchase” journey with sorting.
 * Covers:
 * - Login
 * - Sorting products (Low → High)
 * - Adding two products from landing page
 * - Viewing cart products
 * - Filling checkout form
 * - Completing the order
 * - Logging out
 */
public class SuccessfulPurchaseWithSortingTest extends BaseTest {

    private final String firstProduct = "Sauce Labs Onesie";
    private final String secondProduct = "Sauce Labs Bike Light";

    /**
     * Purpose: Verify a user can log in successfully and land on the Products page.
     * Steps:
     * - Navigate to base URL
     * - Enter valid username & password
     * - Validate that the Products header is displayed
     * Assertion:
     * - Page title equals "Products"
     */
    @Test(description = "Login → Verify Products page is loaded")
    public void login_shouldLandOnProductsPage() {
        String title = new LoginPage(bot)
                .navigateTo(cfg.baseUrl)
                .login(cfg.username, cfg.password)
                .getPageTitle();
        Assert.assertEquals(title, "Products", "User should land on Products page after login.");
    }

    /**
     * Purpose: Confirm that sorting from Low → High price works correctly.
     * Steps:
     * - Select the "Price (low to high)" sort option
     * - Retrieve all product prices
     * Assertion:
     * - Actual order matches expected ascending order
     */
    @Test(dependsOnMethods = "login_shouldLandOnProductsPage",
            description = "Sort products by Price: Low → High")
    public void products_shouldBeSortedFromLowToHigh() {
        boolean sorted = new ProductsPage(bot).sortProductPriceFromLowToHigh();
        Assert.assertTrue(sorted, "Product prices must be sorted from Low → High.");
    }

    /**
     * Purpose: Add two products and confirm that the cart badge increases by two.
     * Steps:
     * - Add first and second product from landing page
     * - Read cart badge value
     * Assertion:
     * - Cart badge count increases by 2
     */
    @Test(dependsOnMethods = {"login_shouldLandOnProductsPage", "products_shouldBeSortedFromLowToHigh"},
            description = "Add two products from landing page and verify cart badge")
    public void addTwoProducts_shouldIncreaseCartCountByTwo() {
        boolean added = new ProductsPage(bot).addProductsAndVerifyCart(firstProduct, secondProduct);
        Assert.assertTrue(added,
                "Both products should be added successfully and cart badge should display 2 items.");
    }

    /**
     * Purpose: Verify that both added products appear correctly inside the cart.
     * Steps:
     * - Open the cart
     * - Validate both product names are displayed
     * Assertion:
     * - Both products are found in the cart
     */
    @Test(dependsOnMethods = {"login_shouldLandOnProductsPage", "products_shouldBeSortedFromLowToHigh", "addTwoProducts_shouldIncreaseCartCountByTwo"},
            description = "Open cart → Verify both products are listed")
    public void cart_shouldDisplayAllAddedProducts() {
        boolean bothPresent = new ProductsPage(bot)
                .openCart()
                .areProductsInCart(firstProduct, secondProduct);

        Assert.assertTrue(bothPresent,
                firstProduct + " and " + secondProduct + " should both be present in the cart.");
    }

    /**
     * Purpose: Complete the checkout information step and reach the order overview.
     * Steps:
     * - Click Checkout
     * - Fill in first name, last name, and postal code
     * - Continue to Overview page
     * Assertion:
     * - Overview page lists the same 2 products
     */
    @Test(dependsOnMethods = {"login_shouldLandOnProductsPage", "products_shouldBeSortedFromLowToHigh", "addTwoProducts_shouldIncreaseCartCountByTwo",
            "cart_shouldDisplayAllAddedProducts"},
            description = "Checkout step → Fill info and verify overview count")
    public void checkout_shouldDisplayTwoItemsInOverview() {
        int overviewCount = new CartPage(bot)
                .startCheckout()
                .fillAndContinue("Jessy", "Roy", "123")
                .overviewItemCount();

        Assert.assertEquals(overviewCount, 2,
                "Overview should list exactly the two added products.");
    }

    /**
     * Purpose: Finish the checkout flow and verify the success message.
     * Steps:
     * - Click Finish
     * - Wait for confirmation message
     * Assertion:
     * - “Thank you for your order!” message is visible
     */
    @Test(dependsOnMethods = {"login_shouldLandOnProductsPage", "products_shouldBeSortedFromLowToHigh", "addTwoProducts_shouldIncreaseCartCountByTwo",
            "cart_shouldDisplayAllAddedProducts", "checkout_shouldDisplayTwoItemsInOverview"},
            description = "Finish order → Verify order completion message")
    public void order_shouldDisplayCompletionMessage() {
        boolean completed = new CheckoutOverviewPage(bot)
                .finishOrder()
                .isOrderComplete();
        Assert.assertTrue(completed,
                "Order completion message 'Thank you for your order!' should be displayed.");
    }

    /**
     * Purpose: Verify user can log out successfully and is redirected to the login page.
     * Steps:
     * - Open side menu
     * - Click Logout
     * - Validate login page UI is visible again
     * Assertion:
     * - Login page elements (like login button) are displayed
     */
    @Test(dependsOnMethods = {"login_shouldLandOnProductsPage", "products_shouldBeSortedFromLowToHigh", "addTwoProducts_shouldIncreaseCartCountByTwo",
            "cart_shouldDisplayAllAddedProducts", "checkout_shouldDisplayTwoItemsInOverview", "order_shouldDisplayCompletionMessage"},
            description = "Logout → Verify redirection to login page")
    public void logout_shouldReturnUserToLoginPage() {
        boolean atLogin = new ProductsPage(bot)
                .logout()
                .isAtLoginPage();
        Assert.assertTrue(atLogin,
                "User should be redirected to the login page after logout.");
    }
}