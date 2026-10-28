@login
Feature: Login Screen Functionality
  As a registered SauceLabs Demo App user
  I want to authenticate via the Login screen
  So that I can access the product catalog and shopping features

  Background:
    Given the user is on the Login screen

  # ─────────────────────────────────────────────────────────────
  # 1. Successful login
  # ─────────────────────────────────────────────────────────────
  @smoke @login @TC1
  Scenario: Successful login with valid credentials
    When  the user enters valid credentials
    And   the user taps the LOGIN button
    Then  the user should be navigated to the Products screen

  # ─────────────────────────────────────────────────────────────
  # 2. Locked out user account validation
  # ─────────────────────────────────────────────────────────────
  @regression @login
  Scenario: Unsuccessful login with a locked out user account
    When  the user enters a locked out username
    And   the user taps the LOGIN button expecting failure
    Then  an error message "Sorry this user has been locked out." should be displayed

  # ─────────────────────────────────────────────────────────────
  # 3. Explicit locked out credentials validation
  # ─────────────────────────────────────────────────────────────
  @regression @login
  Scenario: Unsuccessful login with explicit locked out user credentials
    When  the user enters username "alice@example.com" and password "10203040"
    And   the user taps the LOGIN button expecting failure
    Then  an error message "Sorry this user has been locked out." should be displayed

  # ─────────────────────────────────────────────────────────────
  # 4. Empty username field validation
  # ─────────────────────────────────────────────────────────────
  @regression @login
  Scenario: Login attempt shows validation error when username is empty
    When  the user taps LOGIN with empty username
    Then  a username validation error should be shown

  # ─────────────────────────────────────────────────────────────
  # 5. Empty password field validation
  # ─────────────────────────────────────────────────────────────
  @regression @login
  Scenario: Login attempt shows validation error when password is empty
    When  the user taps LOGIN with empty password
    Then  a password validation error should be shown
