# OpenWay Task — Scenario A: Mail Deletion Testing

## Overview
This project contains:
1. **Test Documentation** (LaTeX) — 20 comprehensive test cases for mail deletion functionality
2. **Automated Tests** (Java + Selenium + TestNG) — Login to Gmail and log the last unread email

## Project Structure
```
OpenWayTask/
├── docs/
│   └── test_documentation.tex          # LaTeX test documentation (20 test cases)
├── src/test/java/com/openway/gmail/
│   ├── base/
│   │   └── BaseTest.java               # WebDriver lifecycle management
│   ├── config/
│   │   └── TestConfig.java             # Credentials & config reader
│   ├── listeners/
│   │   └── TestListener.java           # Custom TestNG listener for logging
│   ├── pages/
│   │   ├── LoginPage.java              # Page Object for Gmail login
│   │   └── InboxPage.java              # Page Object for Gmail inbox
│   └── tests/
│       ├── GmailLoginTest.java         # Login + read last unread email
│       └── GmailDeleteTest.java        # Mail deletion tests
├── src/test/resources/
│   ├── testng.xml                      # TestNG suite configuration
│   └── credentials.env.example         # Credential template
├── pom.xml                             # Maven project configuration
├── mvnw                                # Maven wrapper (auto-downloads Maven)
└── README.md
```

## Prerequisites
- **Java 17+** (JDK) installed
- **Google Chrome** (latest stable version) installed
- A **Gmail test account** with:
  - At least **5 unread emails**
  - **2-Step Verification disabled** (or use an App Password)

## Setup & Running

### 1. Set Credentials
```bash
export GMAIL_EMAIL="your-test-email@gmail.com"
export GMAIL_PASSWORD="your-test-password"
```

### 2. Run Tests
```bash
# Make the Maven wrapper executable
chmod +x mvnw

# Run all tests
./mvnw clean test

# Or pass credentials as system properties
./mvnw clean test -Dgmail.email="test@gmail.com" -Dgmail.password="password"
```

### 3. Compile LaTeX Documentation
```bash
cd docs/
pdflatex test_documentation.tex
pdflatex test_documentation.tex  # Run twice for table of contents
```
Or upload `test_documentation.tex` to [Overleaf](https://www.overleaf.com/) for online compilation.

## Handling Google Verification

Google may ask for additional verification when logging in from a new computer. To handle this:

1. **Disable 2-Step Verification** on the test account (Settings → Security)
2. **Use App Passwords**: If 2FA is required, generate an App Password at [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
3. **Pre-authorize the machine**: Log in manually once from Chrome on the test machine
4. **Allow less secure apps**: Enable this in security settings if available

## Technology Stack
| Component       | Technology                  |
|----------------|-----------------------------|
| Language        | Java 17                     |
| Web Automation  | Selenium WebDriver 4.18.1   |
| Test Framework  | TestNG 7.9.0                |
| Driver Mgmt    | WebDriverManager 5.7.0      |
| Build Tool      | Maven 3.9.6 (via wrapper)   |
| Browser         | Google Chrome               |
| Documentation   | LaTeX                       |