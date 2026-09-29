package com.saucelabs.pages;

import com.saucelabs.utilities.GestureUtils;
import com.saucelabs.utilities.WaitUtils;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Screen Object Model for the Products (catalog) screen.
 *
 * <p>Covers the catalog list, product detail view, sort filter, and cart badge.
 * All locators use {@code accessibilityId} or {@code androidUIAutomator} selectors.
 */
public class ProductsPage extends BasePage {

    private static final Logger LOG = LoggerFactory.getLogger(ProductsPage.class);

    // =========================================================================
    // Locators
    // =========================================================================

    @AndroidFindBy(accessibility = "test-PRODUCTS")
    private WebElement productsHeader;

    /** All product title text elements in the catalog list. */
    @AndroidFindBy(accessibility = "test-Item title")
    private List<WebElement> productTitles;

    /** All product price text elements in the catalog list. */
    @AndroidFindBy(accessibility = "test-Price")
    private List<WebElement> productPrices;

    /** The sort / filter button (funnel icon) in the top-right. */
    @AndroidFindBy(accessibility = "test-Modal Selector Button")
    private WebElement sortButton;

    /** The shopping cart icon with badge. */
    @AndroidFindBy(accessibility = "test-Cart")
    private WebElement cartIcon;

    /** Cart badge counter element. */
    @AndroidFindBy(uiAutomator = "new UiSelector().resourceIdMatches(\".*cartBadge\")")
    private WebElement cartBadge;

    /** "Add to cart" buttons (one per product card). */
    @AndroidFindBy(accessibility = "test-ADD TO CART")
    private List<WebElement> addToCartButtons;

    // ─── Detail Page ─────────────────────────────────────────────────────────

    @AndroidFindBy(accessibility = "test-Description")
    private WebElement productDescription;

    @AndroidFindBy(accessibility = "test-Price")
    private WebElement productDetailPrice;

    @AndroidFindBy(accessibility = "test-ADD TO CART")
    private WebElement addToCartDetailButton;

    @AndroidFindBy(accessibility = "test-BACK TO PRODUCTS")
    private WebElement backToProductsButton;

    // ─── Sort Modal ───────────────────────────────────────────────────────────

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Price (low to high)\")")
    private WebElement sortPriceLowToHigh;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Price (high to low)\")")
    private WebElement sortPriceHighToLow;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Name (A to Z)\")")
    private WebElement sortNameAToZ;

    // =========================================================================
    // Constructor
    // =========================================================================

    public ProductsPage() {
        super();
        LOG.debug("ProductsPage constructed.");
    }

    // =========================================================================
    // Verification Helpers
    // =========================================================================

    /**
     * Returns {@code true} if the Products header is visible.
     */
    public boolean isProductsHeaderDisplayed() {
        return isDisplayed(productsHeader);
    }

    /**
     * Returns a list of all visible product title strings.
     */
    public List<String> getProductTitles() {
        WaitUtils.waitForVisibilityOfAll(productTitles);
        return productTitles.stream()
                .map(WebElement::getText)
                .toList();
    }

    /**
     * Returns a list of all visible product price strings.
     */
    public List<String> getProductPrices() {
        WaitUtils.waitForVisibilityOfAll(productPrices);
        return productPrices.stream()
                .map(WebElement::getText)
                .toList();
    }

    /**
     * Returns {@code true} if at least one product title is displayed.
     */
    public boolean areProductsDisplayed() {
        try {
            WaitUtils.waitForVisibilityOfAll(productTitles);
            return !productTitles.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================================
    // Navigation — Product Detail
    // =========================================================================

    /**
     * Taps on the first product in the list to open its detail view.
     *
     * @return this ProductsPage (re-initialised after navigation)
     */
    public ProductsPage tapFirstProduct() {
        LOG.info("Tapping first product: '{}'", productTitles.get(0).getText());
        click(productTitles.get(0));
        return new ProductsPage();
    }

    /**
     * Returns the product description text on the detail page.
     */
    public String getProductDescription() {
        return getText(productDescription);
    }

    /**
     * Returns the product price on the detail page.
     */
    public String getProductDetailPrice() {
        return getText(productDetailPrice);
    }

    /**
     * Taps BACK TO PRODUCTS and returns a fresh ProductsPage.
     */
    public ProductsPage tapBackToProducts() {
        click(backToProductsButton);
        return new ProductsPage();
    }

    // =========================================================================
    // Scroll (W3C Gestures)
    // =========================================================================

    /**
     * Scrolls down to find a product with the given title text.
     *
     * @param productName product title to search for (case-insensitive substring)
     * @param maxSwipes   maximum swipes before giving up
     * @return this ProductsPage
     */
    public ProductsPage scrollToProduct(String productName, int maxSwipes) {
        LOG.info("Scrolling to product: '{}'", productName);
        for (int i = 0; i < maxSwipes; i++) {
            for (WebElement title : productTitles) {
                try {
                    if (title.getText().equalsIgnoreCase(productName) && title.isDisplayed()) {
                        LOG.info("Product '{}' found after {} swipes.", productName, i);
                        return this;
                    }
                } catch (Exception ignored) {}
            }
            GestureUtils.scrollDown(0.35);
        }
        LOG.warn("Product '{}' not found after {} swipes.", productName, maxSwipes);
        return this;
    }

    /**
     * Returns {@code true} if a product with the given title is currently visible.
     */
    public boolean isProductVisible(String productName) {
        return productTitles.stream()
                .anyMatch(el -> {
                    try {
                        return el.isDisplayed() && el.getText().equalsIgnoreCase(productName);
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    // =========================================================================
    // Cart
    // =========================================================================

    /**
     * Taps "Add to Cart" for the first product in the catalog.
     *
     * @return this ProductsPage (fluent)
     */
    public ProductsPage addFirstProductToCart() {
        LOG.info("Adding first product to cart.");
        click(addToCartButtons.get(0));
        return this;
    }

    /**
     * Returns the cart badge counter text (e.g., "1", "2").
     */
    public String getCartBadgeCount() {
        return getText(cartBadge);
    }

    /**
     * Returns {@code true} if the cart badge is displayed.
     */
    public boolean isCartBadgeDisplayed() {
        return isDisplayed(cartBadge);
    }

    // =========================================================================
    // Sorting
    // =========================================================================

    /**
     * Opens the sort modal.
     *
     * @return this ProductsPage (fluent)
     */
    public ProductsPage openSortModal() {
        LOG.info("Opening sort/filter modal.");
        click(sortButton);
        return this;
    }

    /**
     * Selects "Price (low to high)" from the sort modal.
     *
     * @return this ProductsPage (fluent)
     */
    public ProductsPage selectSortPriceLowToHigh() {
        LOG.info("Selecting sort: Price (low to high).");
        click(sortPriceLowToHigh);
        return new ProductsPage();
    }

    /**
     * Selects "Price (high to low)" from the sort modal.
     *
     * @return this ProductsPage (fluent)
     */
    public ProductsPage selectSortPriceHighToLow() {
        LOG.info("Selecting sort: Price (high to low).");
        click(sortPriceHighToLow);
        return new ProductsPage();
    }

    /**
     * Selects "Name (A to Z)" from the sort modal.
     *
     * @return this ProductsPage (fluent)
     */
    public ProductsPage selectSortNameAToZ() {
        LOG.info("Selecting sort: Name (A to Z).");
        click(sortNameAToZ);
        return new ProductsPage();
    }

    // =========================================================================
    // BasePage contract
    // =========================================================================

    @Override
    public String getPageTitle() {
        return "Products Page";
    }
}
