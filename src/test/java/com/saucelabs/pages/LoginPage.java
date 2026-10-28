package com.saucelabs.pages;

import io.appium.java_client.pagefactory.AndroidFindAll;
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

    @AndroidFindBy(xpath = "//*[@resource-id='com.saucelabs.mydemoapp.android:id/menuIV' or @content-desc='View menu' or @content-desc='test-Menu']")
    private WebElement menuButton;

    @AndroidFindBy(xpath = "//*[@text='Log In' or @content-desc='Login Menu Item' or @content-desc='test-LOG IN']")
    private WebElement loginMenuItem;

    @AndroidFindBy(xpath = "//*[@resource-id='com.saucelabs.mydemoapp.android:id/nameET' or @content-desc='test-Username']")
    private WebElement usernameField;

    @AndroidFindBy(xpath = "//*[@resource-id='com.saucelabs.mydemoapp.android:id/passwordET' or @content-desc='test-Password']")
    private WebElement passwordField;

    @AndroidFindBy(xpath = "//*[@resource-id='com.saucelabs.mydemoapp.android:id/loginBtn' or @content-desc='test-LOGIN']")
    private WebElement loginButton;

    @AndroidFindBy(xpath = "//*[@resource-id='com.saucelabs.mydemoapp.android:id/passwordErrorTV' or @content-desc='test-Error message']")
    private WebElement errorMessage;

    @AndroidFindBy(xpath = "//*[@resource-id='com.saucelabs.mydemoapp.android:id/mTvTitle' or @resource-id='com.saucelabs.mydemoapp.android:id/productTV' or @content-desc='test-PRODUCTS']")
    private WebElement productsHeader;

    @AndroidFindBy(xpath = "//*[@resource-id='com.saucelabs.mydemoapp.android:id/nameErrorTV']")
    private WebElement usernameErrorMessage;

    @AndroidFindBy(xpath = "//*[@resource-id='com.saucelabs.mydemoapp.android:id/passwordErrorTV']")
    private WebElement passwordErrorMessage;

    // =========================================================================
    // Constructor
    // =========================================================================

    public LoginPage() {
        super();
        LOG.debug("LoginPage constructed.");
    }

    /**
     * Ensures driver is on the Login screen. If on Products/Catalog screen,
     * opens side menu and taps 'Log In'.
     */
    public LoginPage navigateToLoginScreen() {
        LOG.info("Checking if already on Login screen...");
        if (isUsernameFieldDisplayed()) {
            LOG.info("Already on Login screen.");
            return this;
        }

        LOG.info("Navigating to Login screen via side menu...");
        try {
            click(menuButton);
            click(loginMenuItem);
            LOG.info("Successfully navigated to Login screen.");
        } catch (Exception e) {
            LOG.warn("Could not navigate to Login screen via menu: {}", e.getMessage());
        }
        return this;
    }

    private boolean isUsernameFieldDisplayed() {
        try {
            return usernameField != null && usernameField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
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
