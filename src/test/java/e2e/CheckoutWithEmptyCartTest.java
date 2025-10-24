package e2e;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.web.CartPage;
import pages.web.LoginPage;
import pages.web.ProductsPage;

/**
 * Scenario S3 — Checkout of empty cart + view cart + logout
 * Flow:
 * 1) Login and land on Products page
 * 2) Open the cart and verify it is empty
 * 3) Ensure Checkout is disabled when the cart is empty
 * 4) Logout and verify we’re back on the login page
 */
public class CheckoutWithEmptyCartTest extends BaseTest {

    /**
     * Purpose: Verify a valid user can log in and land on the Products screen.
     * Steps:
     * - Navigate to the base URL
     * - Submit valid credentials
     * - Read the page title
     * Assert:
     * - Title equals "Products"
     */
    @Test(description = "Login → Products header is visible")
    public void login_shouldLandOnProductsPage() {
        String title = new LoginPage(bot)
                .navigateTo(cfg.baseUrl)
                .login(cfg.username, cfg.password)
                .getPageTitle();
        Assert.assertEquals(title, "Products", "Should land on Products page");
    }

    /**
     * Purpose: Confirm that a fresh session starts with an empty cart.
     * Steps:
     * - From Products page, open the cart
     * - Read the cart count (badge or count)
     * Assert:
     * - Count equals 0
     */
    @Test(dependsOnMethods = "login_shouldLandOnProductsPage",
            description = "Empty cart on fresh session")
    public void cart_shouldBeEmptyAfterFreshLogin() {
        int cartItems = new ProductsPage(bot)
                .openCart()
                .getCartCount();
        Assert.assertEquals(cartItems, 0, "Cart should be empty upon initial login");
    }

    /**
     * Purpose: Ensure Checkout cannot be started when the cart is empty.
     * Steps:
     * - On Cart page with 0 items, check the enabled/disabled state of the Checkout button
     * Assert:
     * - Checkout button is disabled when cart count is 0
     */
    @Test(dependsOnMethods = {"login_shouldLandOnProductsPage", "cart_shouldBeEmptyAfterFreshLogin"},
            description = "Checkout disabled when cart has 0 items")
    public void checkout_shouldBeDisabledWhenCartIsEmpty() {
        boolean isCheckoutEnabled = new CartPage(bot).isCheckoutButtonEnabled();
        Assert.assertFalse(isCheckoutEnabled, "Checkout button should be disabled when cart is empty");
    }

    /**
     * Purpose: Verify a user can log out and is returned to the login screen.
     * Steps:
     * - Open the menu and click Logout
     * - Verify presence of login page UI (e.g., login button/input)
     * Assert:
     * - We are on the login page
     */
    @Test(dependsOnMethods = "login_shouldLandOnProductsPage",
            description = "Logout returns to login page")
    public void logout_shouldRedirectToLoginPage() {
        boolean onLogin = new ProductsPage(bot)
                .logout()
                .isAtLoginPage();
        Assert.assertTrue(onLogin, "User should be redirected to the login page after logout");
    }
}