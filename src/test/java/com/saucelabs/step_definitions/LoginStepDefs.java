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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions bound to {@code Login.feature} scenarios.
 *
 * <p>Each step method obtains the page object as needed; state is maintained
 * via instance fields scoped to the Cucumber glue object lifecycle.
 */
public class LoginStepDefs {

    private static final Logger LOG = LoggerFactory.getLogger(LoginStepDefs.class);

    private LoginPage   loginPage;
    private ProductsPage productsPage;

    // =========================================================================
    // Given
    // =========================================================================

    @Given("the user is on the Login screen")
    public void theUserIsOnTheLoginScreen() {
        LOG.info("Step: the user is on the Login screen");
        loginPage = new LoginPage();
        loginPage.navigateToLoginScreen();
    }

    // =========================================================================
    // When
    // =========================================================================

    @When("the user enters valid credentials")
    public void theUserEntersValidCredentials() {
        String username = ConfigurationReader.get("valid.username");
        String password = ConfigurationReader.get("valid.password");
        LOG.info("Step: entering valid credentials ({})", username);
        loginPage.enterUsername(username)
                 .enterPassword(password);
    }

    @When("the user enters username {string} and password {string}")
    public void theUserEntersUsernameAndPassword(String username, String password) {
        LOG.info("Step: entering username='{}', password='[REDACTED]'", username);
        loginPage.enterUsername(username)
                 .enterPassword(password);
    }

    @When("the user enters a locked out username")
    public void theUserEntersALockedOutUsername() {
        String lockedUser = ConfigurationReader.getOrDefault("locked.username", "alice@example.com");
        String password   = ConfigurationReader.get("valid.password");
        LOG.info("Step: entering locked out username '{}'", lockedUser);
        loginPage.enterUsername(lockedUser)
                 .enterPassword(password);
    }

    @When("the user enters a valid username and an invalid password")
    public void theUserEntersAValidUsernameAndAnInvalidPassword() {
        String username       = ConfigurationReader.get("valid.username");
        String invalidPassword = ConfigurationReader.get("invalid.password");
        LOG.info("Step: entering valid username with invalid password.");
        loginPage.enterUsername(username)
                 .enterPassword(invalidPassword);
    }

    @When("the user enters an unregistered email")
    public void theUserEntersAnUnregisteredEmail() {
        String email    = ConfigurationReader.get("unregistered.email");
        String password = ConfigurationReader.get("valid.password");
        LOG.info("Step: entering unregistered email '{}'", email);
        loginPage.enterUsername(email)
                 .enterPassword(password);
    }

    @When("the user taps the LOGIN button")
    public void theUserTapsTheLoginButton() {
        LOG.info("Step: tapping Login button.");
        productsPage = loginPage.tapLoginButton();
    }

    @When("the user taps the LOGIN button expecting failure")
    public void theUserTapsTheLoginButtonExpectingFailure() {
        LOG.info("Step: tapping Login button (expecting failure).");
        loginPage = loginPage.tapLoginButtonExpectingFailure();
    }

    @When("the user taps LOGIN with empty username")
    public void theUserTapsLoginWithEmptyUsername() {
        LOG.info("Step: tapping Login with empty username.");
        loginPage.enterPassword(ConfigurationReader.get("valid.password"));
        loginPage = loginPage.tapLoginButtonExpectingFailure();
    }

    @When("the user taps LOGIN with empty password")
    public void theUserTapsLoginWithEmptyPassword() {
        LOG.info("Step: tapping Login with empty password.");
        loginPage.enterUsername(ConfigurationReader.get("valid.username"));
        loginPage = loginPage.tapLoginButtonExpectingFailure();
    }

    // =========================================================================
    // Then
    // =========================================================================

    @Then("the user should be navigated to the Products screen")
    public void theUserShouldBeNavigatedToTheProductsScreen() {
        LOG.info("Step: verifying navigation to Products screen.");
        assertNotNull(productsPage, "ProductsPage should not be null after successful login.");
        assertTrue(productsPage.isProductsHeaderDisplayed(),
                "Expected Products header to be displayed after login.");
    }

    @Then("an error message {string} should be displayed")
    public void anErrorMessageShouldBeDisplayed(String expectedMessage) {
        LOG.info("Step: verifying error message contains '{}'.", expectedMessage);
        assertTrue(loginPage.isErrorMessageDisplayed(),
                "Expected error message to be displayed.");
        String actual = loginPage.getErrorMessage();
        assertTrue(actual.contains(expectedMessage),
                "Error message mismatch. Expected to contain: '" + expectedMessage +
                "' but was: '" + actual + "'");
    }

    @Then("a username validation error should be shown")
    public void aUsernameValidationErrorShouldBeShown() {
        LOG.info("Step: verifying username validation error.");
        String errorMsg = loginPage.getUsernameErrorMessage();
        assertFalse(errorMsg.isBlank(), "Username validation error message should not be blank.");
        LOG.info("Username validation error: '{}'", errorMsg);
    }

    @Then("a password validation error should be shown")
    public void aPasswordValidationErrorShouldBeShown() {
        LOG.info("Step: verifying password validation error.");
        String errorMsg = loginPage.getPasswordErrorMessage();
        assertFalse(errorMsg.isBlank(), "Password validation error message should not be blank.");
        LOG.info("Password validation error: '{}'", errorMsg);
    }
}
