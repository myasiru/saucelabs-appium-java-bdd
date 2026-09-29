@products
Feature: Product Catalog Screen Functionality
  As an authenticated SauceLabs Demo App user
  I want to browse, search, sort, and add products to the cart
  So that I can evaluate the full e-commerce journey

  Background:
    Given the user is logged in and on the Products screen

  # ─────────────────────────────────────────────────────────────
  # 1. Default catalog display
  # ─────────────────────────────────────────────────────────────
  @smoke @products
  Scenario: Verify default product catalog items display titles and prices
    Then  at least 3 product titles should be displayed
    And   each product should show a price

  # ─────────────────────────────────────────────────────────────
  # 2. Product detail page
  # ─────────────────────────────────────────────────────────────
  @regression @products
  Scenario: Navigate to a product detail page and verify item details
    When  the user taps on the first product
    Then  the product detail page should display a description
    And   the product detail page should display a price

  # ─────────────────────────────────────────────────────────────
  # 3. Scroll to non-visible item (W3C Gestures)
  # ─────────────────────────────────────────────────────────────
  @regression @products
  Scenario: Scroll down to locate a non-visible product using W3C gesture actions
    When  the user scrolls down to find "Sauce Labs Onesie"
    Then  the product "Sauce Labs Onesie" should be visible

  # ─────────────────────────────────────────────────────────────
  # 4. Add to cart
  # ─────────────────────────────────────────────────────────────
  @regression @products
  Scenario: Add a product to the shopping cart and verify cart badge updates
    When  the user adds the first product to the cart
    Then  the cart badge should display "1"

  # ─────────────────────────────────────────────────────────────
  # 5. Sort by price
  # ─────────────────────────────────────────────────────────────
  @regression @products
  Scenario: Apply Price Low to High sorting filter and verify product order
    When  the user sorts products by "Price: Low to High"
    Then  the products should be ordered from lowest to highest price
