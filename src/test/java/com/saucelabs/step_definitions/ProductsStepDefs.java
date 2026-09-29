package com.saucelabs.step_definitions;

import com.saucelabs.pages.LoginPage;
import com.saucelabs.pages.ProductsPage;
import com.saucelabs.utilities.ConfigurationReader;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions bound to {@code Products.feature} scenarios.
 *
 * <p>State (page objects) is maintained per-scenario via instance fields,
 * consistent with Cucumber's glue object lifecycle.
 */
public class ProductsStepDefs {

    private static final Logger LOG = LoggerFactory.getLogger(ProductsStepDefs.class);

    private ProductsPage productsPage;

    // =========================================================================
    // Given — shared login precondition
    // =========================================================================

    @Given("the user is logged in and on the Products screen")
    public void theUserIsLoggedInAndOnTheProductsScreen() {
        LOG.info("Step: logging in and navigating to Products screen.");
        LoginPage loginPage = new LoginPage();
        productsPage = loginPage
                .enterUsername(ConfigurationReader.get("valid.username"))
                .enterPassword(ConfigurationReader.get("valid.password"))
                .tapLoginButton();
    }

    // =========================================================================
    // Scenario 1 — Catalog display
    // =========================================================================

    @Then("at least {int} product titles should be displayed")
    public void atLeastProductTitlesShouldBeDisplayed(int minCount) {
        LOG.info("Step: verifying at least {} product titles are visible.", minCount);
        List<String> titles = productsPage.getProductTitles();
        assertFalse(titles.isEmpty(), "Product titles list should not be empty.");
        assertTrue(titles.size() >= minCount,
                "Expected at least " + minCount + " titles but found: " + titles.size());
        LOG.info("Visible product titles: {}", titles);
    }

    @And("each product should show a price")
    public void eachProductShouldShowAPrice() {
        LOG.info("Step: verifying each product has a price.");
        List<String> prices = productsPage.getProductPrices();
        assertFalse(prices.isEmpty(), "Price list should not be empty.");
        prices.forEach(price ->
                assertFalse(price.isBlank(), "A product price should not be blank."));
        LOG.info("Visible product prices: {}", prices);
    }

    // =========================================================================
    // Scenario 2 — Product detail
    // =========================================================================

    @When("the user taps on the first product")
    public void theUserTapsOnTheFirstProduct() {
        LOG.info("Step: tapping on first product.");
        productsPage = productsPage.tapFirstProduct();
    }

    @Then("the product detail page should display a description")
    public void theProductDetailPageShouldDisplayADescription() {
        String description = productsPage.getProductDescription();
        LOG.info("Step: product description = '{}'", description);
        assertFalse(description.isBlank(), "Product description should not be blank.");
    }

    @And("the product detail page should display a price")
    public void theProductDetailPageShouldDisplayAPrice() {
        String price = productsPage.getProductDetailPrice();
        LOG.info("Step: product detail price = '{}'", price);
        assertFalse(price.isBlank(), "Product detail price should not be blank.");
        assertTrue(price.startsWith("$"), "Price should start with '$' but was: " + price);
    }

    // =========================================================================
    // Scenario 3 — Scroll to non-visible item
    // =========================================================================

    @When("the user scrolls down to find {string}")
    public void theUserScrollsDownToFind(String productName) {
        LOG.info("Step: scrolling to find product '{}'.", productName);
        productsPage = productsPage.scrollToProduct(productName, 8);
    }

    @Then("the product {string} should be visible")
    public void theProductShouldBeVisible(String productName) {
        LOG.info("Step: verifying product '{}' is visible.", productName);
        assertTrue(productsPage.isProductVisible(productName),
                "Product '" + productName + "' should be visible after scrolling.");
    }

    // =========================================================================
    // Scenario 4 — Add to cart
    // =========================================================================

    @When("the user adds the first product to the cart")
    public void theUserAddsTheFirstProductToTheCart() {
        LOG.info("Step: adding first product to cart.");
        productsPage = productsPage.addFirstProductToCart();
    }

    @Then("the cart badge should display {string}")
    public void theCartBadgeShouldDisplay(String expectedCount) {
        LOG.info("Step: verifying cart badge count = '{}'.", expectedCount);
        assertTrue(productsPage.isCartBadgeDisplayed(), "Cart badge should be visible.");
        String actual = productsPage.getCartBadgeCount();
        assertEquals(expectedCount, actual,
                "Cart badge count mismatch. Expected: '" + expectedCount + "' but was: '" + actual + "'");
    }

    // =========================================================================
    // Scenario 5 — Sort by price
    // =========================================================================

    @When("the user sorts products by {string}")
    public void theUserSortsProductsBy(String sortOption) {
        LOG.info("Step: sorting products by '{}'.", sortOption);
        productsPage = productsPage.openSortModal();
        switch (sortOption.toLowerCase()) {
            case "price: low to high", "price (low to high)" ->
                    productsPage = productsPage.selectSortPriceLowToHigh();
            case "price: high to low", "price (high to low)" ->
                    productsPage = productsPage.selectSortPriceHighToLow();
            case "name (a to z)", "a to z" ->
                    productsPage = productsPage.selectSortNameAToZ();
            default -> throw new IllegalArgumentException("Unknown sort option: " + sortOption);
        }
    }

    @Then("the products should be ordered from lowest to highest price")
    public void theProductsShouldBeOrderedFromLowestToHighestPrice() {
        LOG.info("Step: verifying products are sorted Low to High.");
        List<String> rawPrices = productsPage.getProductPrices();
        List<Double> prices = rawPrices.stream()
                .map(p -> Double.parseDouble(p.replace("$", "").trim()))
                .toList();
        LOG.info("Sorted prices to verify: {}", prices);
        for (int i = 0; i < prices.size() - 1; i++) {
            assertTrue(prices.get(i) <= prices.get(i + 1),
                    String.format("Sort order broken: price[%d]=$%.2f > price[%d]=$%.2f",
                            i, prices.get(i), i + 1, prices.get(i + 1)));
        }
    }
}
