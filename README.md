# 📱 SauceLabs Mobile Test Automation Framework

> **Enterprise-grade** · **Production-ready** · Appium 2.x · Java 21 · Maven · JUnit 5 · Cucumber BDD

[![CI - Smoke Tests](https://github.com/<org>/saucelabs-appium-java-bdd/actions/workflows/ci.yml/badge.svg)](https://github.com/<org>/saucelabs-appium-java-bdd/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://adoptium.net/)
[![Appium](https://img.shields.io/badge/Appium-2.x-purple?logo=appium)](https://appium.io)
[![Cucumber](https://img.shields.io/badge/Cucumber-7.x-green?logo=cucumber)](https://cucumber.io)

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Project Structure](#project-structure)
3. [Prerequisites](#prerequisites)
4. [Environment Setup](#environment-setup)
5. [Configuration](#configuration)
6. [Running Tests](#running-tests)
7. [Test Suites & Tag Strategy](#test-suites--tag-strategy)
8. [Framework Design Decisions](#framework-design-decisions)
9. [CI/CD Strategy](#cicd-strategy)
10. [Reporting](#reporting)
11. [Extending the Framework](#extending-the-framework)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                   CI / Local CLI                            │
│              mvn test -P<profile>                           │
└────────────────────────┬────────────────────────────────────┘
                         │
                    ┌────▼─────┐
                    │  Maven   │  pom.xml profiles + Surefire
                    └────┬─────┘
                         │
              ┌──────────▼──────────┐
              │   JUnit Platform    │  Suite engine
              │    CukesRunner      │
              └──────────┬──────────┘
                         │
           ┌─────────────▼──────────────┐
           │      Cucumber Engine       │
           │  Features → Step Defs      │
           └──┬───────┬────────┬────────┘
              │       │        │
         ┌────▼──┐ ┌──▼───┐ ┌─▼──────┐
         │Hooks  │ │Login │ │Products│  Step Definitions
         │(Before│ │Steps │ │Steps   │
         │/After)│ └──┬───┘ └─┬──────┘
         └────┬──┘    │       │
              │   ┌───▼───────▼───┐
              │   │  Page Objects  │  Screen Object Model
              │   │ (SOM Pattern)  │
              │   │ LoginPage      │
              │   │ ProductsPage   │
              │   │ BasePage       │
              │   └───────┬───────┘
              │           │
         ┌────▼───────────▼──────────┐
         │      Utility Layer        │
         │  Driver  (ThreadLocal)    │
         │  WaitUtils (Explicit/     │
         │            Fluent waits)  │
         │  GestureUtils (W3C Acts)  │
         │  ConfigurationReader      │
         └────────────┬──────────────┘
                      │
              ┌───────▼────────┐
              │ Appium 2.x     │
              │ UiAutomator2   │
              └───────┬────────┘
                      │
              ┌───────▼────────┐
              │ Android Device │
              │ / Emulator     │
              └────────────────┘
```

---

## Project Structure

```
saucelabs-appium-java-bdd/
├── apps/
│   └── SauceLabsApp.apk                 ← Target Android application
├── .github/
│   └── workflows/
│       └── ci.yml                       ← GitHub Actions CI pipeline
├── src/
│   └── test/
│       ├── java/
│       │   └── com/saucelabs/
│       │       ├── pages/
│       │       │   ├── BasePage.java    ← Abstract SOM base with guarded interactions
│       │       │   ├── LoginPage.java   ← Login screen object (Fluent pattern)
│       │       │   └── ProductsPage.java← Products catalog screen object
│       │       ├── runners/
│       │       │   ├── CukesRunner.java ← Primary JUnit Platform Suite runner
│       │       │   └── FailedTestRunner.java ← Re-run failed scenarios runner
│       │       ├── step_definitions/
│       │       │   ├── Hooks.java       ← @Before/@After lifecycle + screenshot
│       │       │   ├── LoginStepDefs.java
│       │       │   └── ProductsStepDefs.java
│       │       └── utilities/
│       │           ├── ConfigurationReader.java ← Singleton property loader
│       │           ├── Driver.java       ← ThreadLocal driver factory (Android/iOS)
│       │           ├── WaitUtils.java    ← Explicit + Fluent wait helpers
│       │           └── GestureUtils.java ← W3C Action gesture library
│       └── resources/
│           ├── features/
│           │   ├── Login.feature        ← 5 login scenarios (BDD)
│           │   └── Products.feature     ← 5 product scenarios (BDD)
│           ├── configuration.properties ← All runtime parameters
│           └── logback-test.xml         ← Structured logging config
└── pom.xml                              ← Maven build descriptor
```

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| **JDK** (Temurin/OpenJDK) | 21+ | Java runtime & compilation |
| **Maven** | 3.9+ | Build & dependency management |
| **Android Studio** | Latest | AVD Manager + SDK tools |
| **Node.js** | 18+ LTS | Appium server runtime |
| **Appium CLI** | 2.x | Mobile automation server |
| **Appium UiAutomator2** | Latest | Android driver plugin |
| **Android SDK** | API 33+ | Emulator system images |

---

## Environment Setup

### 1. Install JDK 21

```bash
# Using SDKMAN (Linux/macOS)
sdk install java 21.0.5-tem

# Windows — download Temurin 21 from https://adoptium.net/
# Set JAVA_HOME and add to PATH
```

### 2. Install Android Studio & SDK

1. Download [Android Studio](https://developer.android.com/studio)
2. Open **SDK Manager** → Install:
   - Android SDK Platform (API 33+)
   - Android Emulator
   - Android SDK Platform-Tools
3. Set environment variables:

```powershell
# Windows PowerShell (add to your profile)
[System.Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
[System.Environment]::SetEnvironmentVariable("Path", "$env:Path;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\emulator", "User")
```

```bash
# macOS / Linux (.zshrc or .bashrc)
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator
```

### 3. Create Android Virtual Device

```bash
# List available system images
sdkmanager --list | grep "system-images"

# Install image
sdkmanager "system-images;android-33;google_apis;x86_64"

# Create AVD
avdmanager create avd \
  --name "Pixel6_API33" \
  --package "system-images;android-33;google_apis;x86_64" \
  --device "pixel_6"

# Start emulator
emulator -avd Pixel6_API33 &
```

### 4. Install Appium 2.x

```bash
# Install Appium globally
npm install -g appium@latest

# Install UiAutomator2 driver
appium driver install uiautomator2

# Verify installation
appium --version
appium driver list --installed

# Run Appium Doctor (optional but recommended)
npx appium-doctor --android
```

### 5. Start Appium Server

```bash
# Start Appium (defaults to http://127.0.0.1:4723)
appium

# Or with explicit options
appium --address 127.0.0.1 --port 4723 --log appium.log
```

---

## Configuration

All runtime parameters live in [`src/test/resources/configuration.properties`](src/test/resources/configuration.properties):

```properties
# Platform: android | ios
platform=android

# Appium Server
appium.server.url=http://127.0.0.1:4723

# Android Device (match your AVD name from: adb devices)
android.deviceName=emulator-5554
android.platformVersion=13.0
android.app=apps/SauceLabsApp.apk

# Timeouts (seconds)
explicit.wait.timeout=20
fluent.wait.timeout=15

# Credentials
valid.username=bob@example.com
valid.password=10203040
```

> **Override at runtime:** `mvn test -Dandroid.deviceName=<device_id>`

---

## Running Tests

### Start the stack first

```bash
# 1. Start your emulator (or connect a real device)
emulator -avd Pixel6_API33 &
adb wait-for-device

# 2. Start Appium server
appium &
```

### Execute via Maven

```bash
# ── Smoke tests only (default)
mvn test

# ── Smoke profile (explicit)
mvn test -Psmoke

# ── Full regression suite
mvn test -Pregression

# ── All tests (excludes @ignore)
mvn test -Pall

# ── Custom tag expression
mvn test -Dcucumber.tags="@login and @smoke"
mvn test -Dcucumber.tags="@products"
mvn test -Dcucumber.tags="not @ignore"

# ── Override device at runtime
mvn test -Psmoke -Dandroid.deviceName=emulator-5556

# ── Parallel execution (2 forks)
mvn test -Pall -Dparallel.threads=2
```

### Re-run failed tests

```bash
# First run generates target/rerun.txt
mvn test -Pregression

# Re-run only failed scenarios
mvn test -Dtest=FailedTestRunner -Dcucumber.features="@target/rerun.txt"
```

---

## Test Suites & Tag Strategy

| Tag | Scope | When to Run |
|-----|-------|-------------|
| `@smoke` | Critical happy paths | Every commit / PR |
| `@regression` | Full feature coverage | Nightly / pre-release |
| `@login` | Login feature group | Login changes |
| `@products` | Products feature group | Catalog / cart changes |
| `@ignore` | Skipped / WIP tests | Never (filtered out) |

---

## Framework Design Decisions

### Thread-Safe Driver (ThreadLocal)

`Driver.java` stores one `AppiumDriver` per OS thread via `ThreadLocal`. This means Cucumber scenarios running in parallel each get their own isolated driver session — no shared state, no race conditions.

### Screen Object Model + Fluent Pattern

```java
// Step definition reads like a sentence:
loginPage
    .enterUsername("bob@example.com")
    .enterPassword("10203040")
    .tapLoginButton()
    .isProductsHeaderDisplayed();
```

Each action method returns the appropriate page object (self or next screen), enabling method chaining without breaking type safety.

### Wait Strategy

- **Implicit waits are disabled** (`implicit.wait.timeout=0`) to avoid masking timing issues.
- All element interactions go through `WaitUtils.waitForClickability()` or `WaitUtils.waitForVisibility()`.
- `FluentWait` with configurable polling is used for asynchronous UI updates.

### W3C Gesture Actions

`GestureUtils.java` uses the `PointerInput` / `Sequence` API exclusively — the W3C standard required by Appium 2.x. Legacy `TouchAction` is not used.

```java
// Scroll to a product
GestureUtils.scrollDownToElement(element, maxSwipes);

// Horizontal swipe
GestureUtils.swipeLeft(0.7);

// Drag and drop
GestureUtils.dragAndDrop(source, target);
```

### Locator Strategy Hierarchy

| Priority | Strategy | When to Use |
|----------|----------|-------------|
| 1 | `accessibilityId` | Always prefer — stable, test-ID driven |
| 2 | `androidUIAutomator` | Complex queries (text, resourceId patterns) |
| 3 | `id` (resourceId) | When accessibility IDs are absent |
| 4 | `xpath` | Last resort only |

---

## CI/CD Strategy

The [GitHub Actions workflow](.github/workflows/ci.yml) runs on every push to `main`/`develop` and every pull request:

```
push / PR
    │
    ▼
┌─────────────────────────────────────────┐
│  GitHub Actions — ubuntu-latest         │
│                                         │
│  1. Checkout + Java 21 + Maven cache    │
│  2. Android SDK + KVM acceleration      │
│  3. Create + Start AVD (API 33)         │
│  4. Node.js + Appium 2.x install        │
│  5. Start Appium server                 │
│  6. Wait for emulator boot              │
│  7. mvn test -Psmoke                    │
│  8. Upload HTML report artifact (14d)   │
│  9. Upload Appium + framework logs (7d) │
└─────────────────────────────────────────┘
```

**Manual trigger** via `workflow_dispatch` supports custom tag expressions.

---

## Reporting

After a test run, reports are generated in `target/cucumber-reports/`:

| File | Format | Description |
|------|--------|-------------|
| `cucumber-report.html` | HTML | Human-readable visual report |
| `cucumber-report.json` | JSON | Machine-readable; Allure/Jenkins compatible |
| `cucumber-report.xml` | JUnit XML | CI dashboard integration |
| `rerun.txt` | Text | List of failed scenario URIs for re-run |

**Open HTML report:**
```bash
start target/cucumber-reports/cucumber-report.html   # Windows
open  target/cucumber-reports/cucumber-report.html   # macOS
```

**Screenshot on failure** — automatically captured and embedded in the HTML report body (no external file references).

---

## Extending the Framework

### Add a New Screen

1. Create `src/test/java/com/saucelabs/pages/CheckoutPage.java` extending `BasePage`.
2. Add `@AndroidFindBy` locators.
3. Implement fluent action methods returning `CheckoutPage` (or next page).

### Add iOS Support

In `configuration.properties`, set `platform=ios` and configure `ios.*` properties. `Driver.java` will automatically create an `IOSDriver` with `XCUITestOptions`.

### Add Cloud (SauceLabs/BrowserStack)

Override `appium.server.url` with the cloud endpoint and add cloud-specific capabilities via a new `createCloudDriver()` branch in `Driver.java`.

---

*Built with ❤️ using Appium 2.x, Java 21, Cucumber 7.x, and JUnit 5.*
