# 📱 SauceLabs Mobile Test Automation Framework

> **Enterprise-grade** · **Production-ready** · Appium 2.x · Java 21 · Maven · JUnit 5 · Cucumber BDD

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://adoptium.net/)
[![Appium](https://img.shields.io/badge/Appium-2.x-purple?logo=appium)](https://appium.io)
[![Cucumber](https://img.shields.io/badge/Cucumber-7.x-green?logo=cucumber)](https://cucumber.io)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-red?logo=junit5)](https://junit.org/junit5/)
[![UiAutomator2](https://img.shields.io/badge/Driver-UiAutomator2-blue?logo=android)](https://github.com/appium/appium-uiautomator2-driver)

---

## 📋 Table of Contents

1. [⚡ Quick Start (5 Minutes to First Run)](#-quick-start-5-minutes-to-first-run)
2. [Architecture Overview](#architecture-overview)
3. [Project Structure](#project-structure)
4. [Prerequisites](#prerequisites)
5. [Environment Setup](#environment-setup)
6. [Configuration](#configuration)
7. [Running Tests](#running-tests)
8. [Test Suites & Tag Strategy](#test-suites--tag-strategy)
9. [Framework Design Decisions](#framework-design-decisions)
10. [CI/CD Strategy](#cicd-strategy)
11. [Reporting](#reporting)
12. [Troubleshooting & Common Issues](#troubleshooting--common-issues)
13. [Extending the Framework](#extending-the-framework)

---

## ⚡ Quick Start (5 Minutes to First Run)

Follow these steps right after cloning the repository:

### 1. Clone the repository
```bash
git clone <repository_url>
cd saucelabs-appium-java-bdd
```

### 2. Verify Prerequisites
Ensure Java 21, Maven, Node.js, and Android platform-tools are installed:
```bash
java -version    # Must be 21+
mvn -version     # Must be 3.9+
node -v          # Must be 18+ LTS
adb version      # Android Debug Bridge
```

### 3. Start Android Emulator
Launch your emulator via Android Studio Device Manager, or via CLI:
```bash
# Start emulator (example: Pixel_7)
emulator -avd Pixel_7

# Verify connected device (should display 'device' status)
adb devices
```

### 4. Install & Start Appium Server
```bash
# 1. Install Appium globally (one-time setup)
npm install -g appium

# 2. Install UiAutomator2 driver (one-time setup)
appium driver install uiautomator2

# 3. Start Appium server (runs on http://127.0.0.1:4723 by default)
appium
```
> Keep this terminal window open while tests are running.

### 5. Execute Tests!
In a separate terminal, run:

- **macOS / Linux (Bash/Zsh):**
  ```bash
  mvn test
  ```
- **Windows (PowerShell):**
  ```powershell
  mvn test "-Dcucumber.tags=@smoke"
  ```
> 💡 *Note: The APK (`apps/SauceLabsApp.apk`) is bundled directly in the repository. No manual build or download is needed!*

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
│   └── SauceLabsApp.apk                 ← Bundled Android target application
├── .github/
│   └── workflows/
│       └── ci.yml                       ← GitHub Actions CI pipeline
├── src/
│   └── test/
│       ├── java/
│       │   └── com/saucelabs/
│       │       ├── debug/
│       │       │   └── PageSourceDumper.java ← Diagnostic utility for XML page dumps
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

| Tool | Version | Purpose | Download / Guide |
|------|---------|---------|------------------|
| **JDK** | 21+ | Java Runtime & Compiler | [Adoptium Temurin 21](https://adoptium.net/) |
| **Maven** | 3.9+ | Build & Dependency Management | [Maven Download](https://maven.apache.org/download.cgi) |
| **Android Studio** | Latest | AVD Manager + Android SDK | [Android Studio](https://developer.android.com/studio) |
| **Node.js** | 18+ LTS | Appium CLI runtime | [Node.js](https://nodejs.org/) |
| **Appium CLI** | 2.x / 3.x | Mobile automation server | `npm install -g appium` |
| **UiAutomator2** | Latest | Android driver | `appium driver install uiautomator2` |
| **Android SDK** | API 33/34 | System images & platform-tools | Installed via Android Studio SDK Manager |

---

## Environment Setup

### 1. Install Java 21
Verify that `JAVA_HOME` points to your JDK 21 installation:
```bash
java -version
```

### 2. Configure Android SDK Environment Variables

#### Windows (PowerShell)
```powershell
[System.Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
[System.Environment]::SetEnvironmentVariable("Path", "$env:Path;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\emulator", "User")
```

#### macOS / Linux (`~/.zshrc` or `~/.bashrc`)
```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator
```

### 3. Verify Android ADB Connection
Ensure your device or emulator is detected:
```bash
adb devices
# Output should be:
# List of devices attached
# emulator-5554    device
```

### 4. Install Appium and Drivers
```bash
# Install Appium CLI
npm install -g appium

# Install UiAutomator2 driver
appium driver install uiautomator2

# Verify installed drivers
appium driver list --installed
# Output should list:
# - uiautomator2@... [installed (npm)]
```

### 5. Start Appium Server
```bash
appium
```
By default, the server listens on `http://127.0.0.1:4723`.

---

## Configuration

All runtime configurations live in [`src/test/resources/configuration.properties`](src/test/resources/configuration.properties):

```properties
# Platform: android | ios
platform=android

# Appium Server URL
appium.server.url=http://127.0.0.1:4723

# Android Device Capabilities
# (Ensure android.deviceName matches your 'adb devices' output)
android.deviceName=emulator-5554
android.platformVersion=14.0
android.apiLevel=34
android.avdName=Pixel_7

# Application Path (relative to project root, auto-resolved)
android.app=apps/SauceLabsApp.apk
android.appPackage=com.saucelabs.mydemoapp.android
android.appActivity=com.saucelabs.mydemoapp.android.view.activities.SplashActivity
android.automationName=UiAutomator2
android.noReset=false
android.fullReset=false
android.newCommandTimeout=60
android.autoGrantPermissions=true
android.chromedriverAutodownload=true

# Wait Timeouts (seconds)
explicit.wait.timeout=20
fluent.wait.timeout=15
fluent.wait.polling=500
implicit.wait.timeout=0

# Test Credentials
valid.username=bob@example.com
valid.password=10203040
invalid.password=wrongPass99
unregistered.email=notfound@example.com
locked.username=alice@example.com

# Reporting
screenshot.on.failure=true
```

> **Runtime Override:** Any configuration property can be overridden from the command line:
> ```bash
> mvn test "-Dandroid.deviceName=emulator-5556"
> ```

---

## Running Tests

> ⚠️ **Important for Windows PowerShell users:**
> In PowerShell, Maven `-D` parameters containing dots (like `-Dcucumber.tags=@smoke`) **must be enclosed in double quotes**:
> `mvn test "-Dcucumber.tags=@smoke"`
> If not quoted, PowerShell splits the argument and Maven throws: `[ERROR] Unknown lifecycle phase ".tags=@smoke"`.

### Run by Tags

```bash
# ── Run smoke tests (default profile)
mvn test

# ── Explicit profile runs
mvn test -Psmoke
mvn test -Pregression
mvn test -Pall

# ── Run by Feature
# All Login tests (5 scenarios)
mvn test "-Dcucumber.tags=@login"

# All Product tests (5 scenarios)
mvn test "-Dcucumber.tags=@products"

# ── Run a Specific Scenario by Test Case ID
# Scenario 1 in Login.feature
mvn test "-Dcucumber.tags=@TC1"

# Scenario 2 in Products.feature
mvn test "-Dcucumber.tags=@TC7"

# Run multiple specific test cases
mvn test "-Dcucumber.tags=@TC7 or @TC9"

# ── Combined Tag Filters
mvn test "-Dcucumber.tags=@smoke and @login"
mvn test "-Dcucumber.tags=@regression and not @ignore"
```

### Re-run Failed Tests
If any tests fail during a test run, Cucumber automatically records the failed scenario lines into `target/rerun.txt`. You can immediately re-run only the failed tests:

```bash
mvn test -Dtest=FailedTestRunner "-Dcucumber.features=@target/rerun.txt"
```

---

## Test Suites & Tag Strategy

| Tag | Target | Description |
|-----|--------|-------------|
| `@smoke` | Critical happy-path scenarios | Runs by default on `mvn test` |
| `@regression` | Full feature validation suite | Detailed edge cases, validations, sorting |
| `@login` | `Login.feature` | All 5 login scenarios |
| `@products` | `Products.feature` | All 5 product catalog & cart scenarios |
| `@TC1` | Login Scenario 1 | Successful login with valid credentials |
| `@TC6` | Products Scenario 1 | Default catalog display (titles & prices) |
| `@TC7` | Products Scenario 2 | Navigate to product detail page |
| `@TC8` | Products Scenario 3 | Scroll down using W3C gesture actions |
| `@TC9` | Products Scenario 4 | Add product to cart & verify badge counter |
| `@TC10`| Products Scenario 5 | Apply price sorting (Low to High) |

---

## Framework Design Decisions

### 1. Thread-Safe Driver (`Driver.java`)
Uses `ThreadLocal<AppiumDriver>` to ensure that each parallel test thread maintains its own driver session. This isolates state and prevents cross-test race conditions.

### 2. Screen Object Model (SOM) + Fluent Pattern
Pages inherit from `BasePage.java` which encapsulates element wait guards. Action methods return page instances enabling fluent chaining:
```java
productsPage = loginPage
        .enterUsername("bob@example.com")
        .enterPassword("10203040")
        .tapLoginButton();
```

### 3. W3C Gesture Actions (`GestureUtils.java`)
Mobile interactions (scrolls, swipes, drags) strictly implement the modern **W3C Actions API** (`PointerInput` and `Sequence`). Legacy, deprecated `TouchAction` APIs are avoided.

### 4. Smart Synchronization (`WaitUtils.java`)
- **Zero Implicit Waits**: Prevents polling delays and conflicting timeout behaviors.
- **Explicit & Fluent Waits**: Used before every click and text retrieval with configurable polling intervals.

---

## CI/CD Strategy

The repository includes a production-ready GitHub Actions workflow in [`.github/workflows/ci.yml`](.github/workflows/ci.yml):
- Configures Java 21, Android SDK, and KVM hardware acceleration.
- Starts an Android emulator (API 33) and Appium server.
- Executes `mvn test -Psmoke`.
- Uploads HTML reports and screenshots as build artifacts.

---

## Reporting

After test execution, reports are generated automatically under `target/cucumber-reports/`:

| Report File | Format | Description |
|-------------|--------|-------------|
| `cucumber-report.html` | HTML | Visual interactive report with embedded failure screenshots |
| `cucumber-report.json` | JSON | Machine-readable report for CI tools (Allure/Jenkins) |
| `rerun.txt` | Text | List of failed scenario line numbers for easy retry |

### View Reports
- **Windows:**
  ```powershell
  Invoke-Item target/cucumber-reports/cucumber-report.html
  ```
- **macOS / Linux:**
  ```bash
  open target/cucumber-reports/cucumber-report.html
  ```

---

## Troubleshooting & Common Issues

### 1. Maven Error: `Unknown lifecycle phase ".tags=@..."`
- **Cause:** Windows PowerShell parses `-Dcucumber.tags=@smoke` as a sub-command.
- **Solution:** Enclose property arguments in quotes:
  ```powershell
  mvn test "-Dcucumber.tags=@smoke"
  ```

### 2. Appium Error: `Connection refused: http://127.0.0.1:4723`
- **Cause:** Appium server is not running.
- **Solution:** Open a new terminal and run `appium`. Wait until you see:
  `[Appium] Appium REST http interface listener started on http://0.0.0.0:4723`

### 3. `Could not find a connected Android device`
- **Cause:** Emulator is not booted or `adb` connection is offline.
- **Solution:**
  1. Run `adb devices`.
  2. If the emulator is `offline`, restart it.
  3. Reset the adb daemon: `adb kill-server && adb start-server`.
  4. Ensure `android.deviceName` in `configuration.properties` matches the device serial from `adb devices`.

### 4. `EPERM: operation not permitted` or Sandbox Permission on Windows
- **Cause:** Command running in a restricted sandbox or shell without proper user permissions to `~/.android`.
- **Solution:** Ensure your command prompt or terminal has standard user privileges and access to `C:\Users\<username>\.android`.

---

## Extending the Framework

### Adding a New Screen
1. Create `src/test/java/com/saucelabs/pages/CartPage.java` extending `BasePage`.
2. Define `@AndroidFindBy` elements using `accessibilityId`, `id`, or `xpath`.
3. Implement fluent user interaction methods.

### Adding New Feature Scenarios
1. Create or edit a `.feature` file in `src/test/resources/features/`.
2. Annotate scenarios with appropriate tags (e.g. `@smoke`, `@regression`, `@TC...`).
3. Implement step definitions in `com.saucelabs.step_definitions`.

---

*Built with ❤️ using Appium 2.x, Java 21, Cucumber 7.x, and JUnit 5.*
