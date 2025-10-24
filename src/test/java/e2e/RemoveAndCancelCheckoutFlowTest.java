package e2e;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.web.CartPage;
import pages.web.LoginPage;
import pages.web.ProductsPage;

/**
 * Scenario S2 — Remove & Cancel Flow
 * Covers:
 * - Login
 * - Add products from landing page
 * - Remove product from landing page
 * - View cart products
 * - Remove product from cart
 * - Canceling checkout (assert item remains)
 * - Logout
 */
public class RemoveAndCancelCheckoutFlowTest extends BaseTest {

    private final String firstProduct = "Sauce Labs Onesie";
    private final String secondProduct = "Sauce Labs Bike Light";

    /**
     * Purpose: Verify a user can log in and land on Products page.
     */
    @Test(description = "Login → Verify Products page is loaded")
    public void login_shouldLandOnProductsPage() {
        String title = new LoginPage(bot)
                .navigateTo(cfg.baseUrl)
                .login(cfg.username, cfg.password)
                .getPageTitle();
        Assert.assertEquals(title, "Products", "Should land on Products page");
    }

    /**
     * Purpose: Add two products from landing and assert cart badge = 2.
     */
    @Test(dependsOnMethods = "login_shouldLandOnProductsPage",
            description = "Add two products from landing → Cart badge should be 2")
    public void addTwoProducts_shouldIncreaseBadgeToTwo() {
        boolean ok = new ProductsPage(bot).addProductsAndVerifyCart(firstProduct, secondProduct);
        Assert.assertTrue(ok, "Both products should be added; Cart badge should show 2 items.");
    }

    /**
     * Purpose: Remove the first product on landing and assert badge decrements by 1.
     */
    @Test(dependsOnMethods = {"addTwoProducts_shouldIncreaseBadgeToTwo"},
            description = "Remove first product on landing → Cart badge decrements by 1")
    public void removeFirstProduct_onLanding_shouldDecrementBadgeByOne() {
        boolean removed = new ProductsPage(bot).removeProductFromLanding(firstProduct);
        Assert.assertTrue(removed, "Cart badge should decrement by 1 after removing the first product on landing.");
    }

    /**
     * Purpose: Remove the second product from the Cart page and assert badge decrements to 0.
     */
    @Test(dependsOnMethods = {"removeFirstProduct_onLanding_shouldDecrementBadgeByOne"},
            description = "Open cart → Remove second product → Cart badge becomes 0")
    public void removeSecondProduct_inCart_shouldMakeBadgeZero() {
        boolean removed = new ProductsPage(bot)
                .openCart()
                .removeProductAndVerifyBadge(secondProduct);
        Assert.assertTrue(removed, "After removing the second product from cart, Cart badge should be 0.");
    }

    /**
     * Purpose: Add one product again, start checkout, cancel from overview, and ensure the item is still in the cart.
     * Flow:
     * Cart (empty) → Continue Shopping → Add one product → Cart → Checkout → Fill Info → Overview → Cancel → Cart
     * Assertion:
     * Cart count should be exactly 1 (the product remains after cancel).
     */
    @Test(dependsOnMethods = {"removeSecondProduct_inCart_shouldMakeBadgeZero"},
            description = "Cancel checkout from overview → Item remains in cart")
    public void cancelCheckout_fromOverview_shouldKeepItemInCart() {
        int cartCount = new CartPage(bot)
                .continueShopping()                // → Products
                .addProduct(firstProduct)          // add 1 item
                .openCart()                        // → Cart
                .startCheckout()                   // → Info
                .fillInfo("Jane", "Doe", "12345")  // fill but DO NOT continue
                .cancelAndReturnToCart()           // cancel from Info step (uses the method)
                .getCartCount();                   // still 1

        Assert.assertEquals(cartCount, 1,
                "After canceling from Info step, the single added item should remain in the cart.");
    }

    /**
     * Purpose: Verify logout returns user to login page.
     */
    @Test(dependsOnMethods = {"cancelCheckout_fromOverview_shouldKeepItemInCart"},
            description = "Logout → Verify redirection to login page")
    public void logout_shouldReturnUserToLoginPage() {
        boolean atLogin = new ProductsPage(bot)
                .logout()
                .isAtLoginPage();
        Assert.assertTrue(atLogin, "User should be redirected to the login page after logout.");
    }
}