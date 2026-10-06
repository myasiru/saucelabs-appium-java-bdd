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
  # 2. Invalid password
  # ─────────────────────────────────────────────────────────────
  @regression @login
  Scenario: Unsuccessful login with an invalid password
    When  the user enters a valid username and an invalid password
    And   the user taps the LOGIN button expecting failure
    Then  an error message "Username and password do not match any user in this service" should be displayed

  # ─────────────────────────────────────────────────────────────
  # 3. Unregistered email
  # ─────────────────────────────────────────────────────────────
  @regression @login
  Scenario: Unsuccessful login with an unregistered email address
    When  the user enters an unregistered email
    And   the user taps the LOGIN button expecting failure
    Then  an error message "Username and password do not match any user in this service" should be displayed

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
