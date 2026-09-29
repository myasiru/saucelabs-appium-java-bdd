package com.saucelabs.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Screen Object Model for the Login screen.
 *
 * <p>Locator strategy uses {@code accessibilityId} and {@code androidUIAutomator}
 * to avoid brittle XPath expressions. Methods return page instances (Fluent Pattern)
 * enabling readable step-definition method chains.
 */
public class LoginPage extends BasePage {

    private static final Logger LOG = LoggerFactory.getLogger(LoginPage.class);

    // =========================================================================
    // Locators — AppiumBy / @AndroidFindBy
    // =========================================================================

    @AndroidFindBy(accessibility = "test-Username")
    private WebElement usernameField;

    @AndroidFindBy(accessibility = "test-Password")
    private WebElement passwordField;

    @AndroidFindBy(accessibility = "test-LOGIN")
    private WebElement loginButton;

    @AndroidFindBy(xpath = "//android.view.ViewGroup[@content-desc='test-Error message']/android.widget.TextView")
    private WebElement errorMessage;

    @AndroidFindBy(accessibility = "test-PRODUCTS")
    private WebElement productsHeader;

    @AndroidFindBy(uiAutomator = "new UiSelector().resourceIdMatches(\".*username-error\")")
    private WebElement usernameErrorMessage;

    @AndroidFindBy(uiAutomator = "new UiSelector().resourceIdMatches(\".*password-error\")")
    private WebElement passwordErrorMessage;

    // =========================================================================
    // Constructor
    // =========================================================================

    public LoginPage() {
        super();
        LOG.debug("LoginPage constructed.");
    }

    // =========================================================================
    // Fluent Action Methods
    // =========================================================================

    /**
     * Enters the username into the username field.
     *
     * @param username username string
     * @return this LoginPage (fluent)
     */
    public LoginPage enterUsername(String username) {
        LOG.info("Entering username: {}", username);
        type(usernameField, username);
        return this;
    }

    /**
     * Enters the password into the password field.
     *
     * @param password password string
     * @return this LoginPage (fluent)
     */
    public LoginPage enterPassword(String password) {
        LOG.info("Entering password: [REDACTED]");
        type(passwordField, password);
        return this;
    }

    /**
     * Taps the LOGIN button and returns a {@link ProductsPage} — expects successful login.
     *
     * @return ProductsPage
     */
    public ProductsPage tapLoginButton() {
        LOG.info("Tapping Login button (expecting success).");
        click(loginButton);
        return new ProductsPage();
    }

    /**
     * Taps the LOGIN button and stays on LoginPage — expects failure / validation error.
     *
     * @return this LoginPage (fluent)
     */
    public LoginPage tapLoginButtonExpectingFailure() {
        LOG.info("Tapping Login button (expecting failure).");
        click(loginButton);
        return this;
    }

    // =========================================================================
    // Assertion Helpers
    // =========================================================================

    /**
     * Returns the text of the generic login error message banner.
     */
    public String getErrorMessage() {
        return getText(errorMessage);
    }

    /**
     * Returns {@code true} if the error message banner is visible.
     */
    public boolean isErrorMessageDisplayed() {
        return isDisplayed(errorMessage);
    }

    /**
     * Returns the username field validation/error message.
     */
    public String getUsernameErrorMessage() {
        return getText(usernameErrorMessage);
    }

    /**
     * Returns the password field validation/error message.
     */
    public String getPasswordErrorMessage() {
        return getText(passwordErrorMessage);
    }

    /**
     * Returns {@code true} if the Products header is visible (login succeeded).
     */
    public boolean isLoginSuccessful() {
        return isDisplayed(productsHeader);
    }

    // =========================================================================
    // BasePage contract
    // =========================================================================

    @Override
    public String getPageTitle() {
        return "Login Page";
    }
}
