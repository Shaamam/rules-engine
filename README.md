# Rules Engine KJAR

A **Drools-based Rules Engine** packaged as a Knowledge JAR (KJAR) for managing business rules across microservices. This POC demonstrates how to externalize business logic into reusable rules that can be consumed by multiple services.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![Drools](https://img.shields.io/badge/Drools-8.44.0-blue)]()
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)]()

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Rules Overview](#rules-overview)
- [Getting Started](#getting-started)
- [Building the Project](#building-the-project)
- [Testing](#testing)
- [Publishing to GitHub Packages](#publishing-to-github-packages)
- [Using in Microservices](#using-in-microservices)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)

## 🎯 Overview

This KJAR contains three main rule sets for e-commerce business logic:

- **Offer Rules** - Dynamic discount calculations based on customer segments and order characteristics
- **Order Rules** - Shipping charges, processing fees, and order prioritization
- **Payment Rules** - Payment validation, risk scoring, and cashback calculations

The rules engine uses **stateless sessions** for thread-safe, high-performance rule execution across distributed microservices.

## ✨ Features

- 🔥 **Hot-reloadable Rules** - Update rules without service restarts
- 🚀 **High Performance** - Stateless sessions for concurrent execution
- 📦 **Modular Design** - Separate rule bases for different domains
- 🧪 **Fully Tested** - 31 comprehensive unit tests
- 📊 **Production Ready** - Proper logging and error handling
- 🔒 **Type Safe** - Lombok-based model classes with validation

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Microservices Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │Offer Service │  │Order Service │  │Payment Service│     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬────────┘     │
│         │                  │                  │              │
│         └──────────────────┼──────────────────┘              │
│                            │                                 │
└────────────────────────────┼─────────────────────────────────┘
                             │
                             ▼
         ┌───────────────────────────────────────┐
         │     Rules Engine KJAR (this repo)     │
         ├───────────────────────────────────────┤
         │  ┌─────────────────────────────────┐  │
         │  │   Offer Rules (offer-rules.drl)  │  │
         │  │   • Customer Segment Discounts   │  │
         │  │   • Category-based Offers        │  │
         │  │   • First-time Customer Deals    │  │
         │  └─────────────────────────────────┘  │
         │  ┌─────────────────────────────────┐  │
         │  │   Order Rules (order-rules.drl)  │  │
         │  │   • Shipping Charges             │  │
         │  │   • Order Prioritization         │  │
         │  │   • Fee Calculations             │  │
         │  └─────────────────────────────────┘  │
         │  ┌─────────────────────────────────┐  │
         │  │ Payment Rules (payment-rules.drl)│  │
         │  │   • Risk Assessment              │  │
         │  │   • Payment Validation           │  │
         │  │   • Cashback Calculations        │  │
         │  └─────────────────────────────────┘  │
         └───────────────────────────────────────┘
```

## 📁 Project Structure

```
rules-engine/
├── src/
│   ├── main/
│   │   ├── java/io/shaama/rulesengine/
│   │   │   ├── RulesengineApplication.java
│   │   │   └── model/
│   │   │       ├── Offer.java         # Offer domain model
│   │   │       ├── Order.java         # Order domain model
│   │   │       └── Payment.java       # Payment domain model
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── kmodule.xml        # Drools configuration
│   │       └── rules/
│   │           ├── offer-rules.drl    # Offer business rules
│   │           ├── order-rules.drl    # Order business rules
│   │           └── payment-rules.drl  # Payment business rules
│   └── test/
│       └── java/io/shaama/rulesengine/
│           ├── OfferRulesTest.java    # 8 offer rule tests
│           ├── OrderRulesTest.java    # 11 order rule tests
│           └── PaymentRulesTest.java  # 11 payment rule tests
├── build.gradle                        # Build configuration
├── settings.gradle
├── README.md                           # This file
└── OFFER_SERVICE_PROMPT.md            # Guide for consuming services
```

## 📚 Rules Overview

### Offer Rules (8 rules)

| Rule | Description | Salience | Conditions |
|------|-------------|----------|------------|
| First Time Customer | 15% discount | 100 | isFirstTimeCustomer, amount ≥ 500 |
| Premium Customer | 20% discount | 90 | segment = PREMIUM, amount ≥ 1000 |
| Gold Customer | 15% discount | 80 | segment = GOLD, amount ≥ 800 |
| Fashion Category | 25% discount | 75 | category = FASHION, amount ≥ 1500 |
| Electronics Category | 10% discount | 70 | category = ELECTRONICS, amount ≥ 2000 |
| Expired Offer | Reject | 110 | offerValidUntil < today |
| Below Minimum | Reject | 105 | orderAmount < 200 |

### Order Rules (12 rules)

| Rule | Description | Salience |
|------|-------------|----------|
| Free Shipping | Orders ≥ 1000 | 100 |
| Local Shipping | 50 charge | 90 |
| Regional Shipping | 100 charge | 90 |
| National Shipping | 200 charge | 90 |
| International Shipping | 500 charge | 90 |
| Peak Hour Fee | 25 processing fee | 85 |
| High Value Approval | Orders ≥ 10,000 require approval | 95 |
| Electronics Priority | High priority for electronics ≥ 5000 | 80 |
| Bulk Orders | Medium priority for 10+ items | 75 |
| Total Calculation | Calculate final amount | 1 |

### Payment Rules (13 rules)

| Rule | Description | Risk Score |
|------|-------------|------------|
| High Value Review | Amount ≥ 50,000 requires manual review | 75 |
| Credit Limit Check | Reject if exceeds limit | 100 |
| COD Limit | Reject COD > 5,000 | - |
| Low Risk Auto-Approve | Regular/VIP, amount < 10,000 | 10 |
| Credit Card Fee | 2.5% transaction fee | - |
| Debit Card Fee | 1.5% transaction fee | - |
| UPI Fee | 0% transaction fee | - |
| VIP Cashback | 5% cashback | - |
| Regular Customer Cashback | 2% on high value | - |

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Gradle 8.x** (wrapper included)
- **Git** for version control

### Clone the Repository

```bash
git clone https://github.com/Shaamam/rules-engine.git
cd rules-engine
```

### Quick Start

```bash
# Build the project
./gradlew clean build

# Run tests
./gradlew test

# Install to local Maven repository
./gradlew publishToMavenLocal
```

## 🔨 Building the Project

### Build Commands

```bash
# Clean build
./gradlew clean build

# Build without tests
./gradlew build -x test

# Run tests only
./gradlew test

# Check for compilation errors
./gradlew compileJava
```

### Build Output

The build produces:
- `build/libs/rules-engine-kjar-1.0.0-SNAPSHOT.jar` - The KJAR artifact
- `build/resources/main/META-INF/kmodule.xml` - Drools configuration
- `build/resources/main/rules/*.drl` - Compiled rule files

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Test Coverage

- **31 tests** covering all rules
- **100% rule coverage** - every rule is tested
- **Fast execution** - all tests complete in ~1 second

### Test Reports

After running tests, view the HTML report:
```bash
open build/reports/tests/test/index.html
```

### Example Test

```java
@Test
void testPremiumCustomerOffer() {
    Offer offer = Offer.builder()
            .offerId("OFF-002")
            .customerId("CUST-002")
            .customerSegment("PREMIUM")
            .orderAmount(new BigDecimal("1500"))
            .isFirstTimeCustomer(false)
            .build();

    kieSession.execute(offer);

    assertTrue(offer.isOfferApplicable());
    assertEquals(new BigDecimal("20"), offer.getDiscountPercentage());
    assertEquals(new BigDecimal("300.00"), offer.getDiscountAmount());
}
```

## 📦 Publishing to GitHub Packages

### Configure GitHub Credentials

```bash
# Set environment variables
export GITHUB_USERNAME=Shaamam
export GITHUB_TOKEN=your_github_personal_access_token
```

Or add to `~/.gradle/gradle.properties`:
```properties
gpr.user=Shaamam
gpr.key=your_github_personal_access_token
```

### Publish

```bash
./gradlew publish
```

### Verify Publication

Check GitHub Packages:
```
https://github.com/Shaamam/rules-engine/packages
```

## 🔌 Using in Microservices

### Add Dependency

**Maven (`pom.xml`)**:
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Shaamam/rules-engine</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.shaama</groupId>
        <artifactId>rules-engine-kjar</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

**Gradle (`build.gradle`)**:
```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/Shaamam/rules-engine")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'io.shaama:rules-engine-kjar:1.0.0-SNAPSHOT'
}
```

### Use in Code

```java
import io.shaama.rulesengine.model.Offer;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;

@Service
public class OfferService {
    
    private final StatelessKieSession kieSession;
    
    public OfferService() {
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        this.kieSession = kieContainer.newStatelessKieSession("offerKSession");
    }
    
    public Offer evaluateOffer(Offer offer) {
        kieSession.execute(offer);
        return offer;
    }
}
```

### Session Names

The KJAR provides these configured sessions (see `kmodule.xml`):
- `offerKSession` - For offer rules
- `orderKSession` - For order rules
- `paymentKSession` - For payment rules
- `allRulesKSession` - All rules combined (default)

## 📖 API Documentation

### Model Classes

#### Offer
```java
Offer offer = Offer.builder()
    .offerId("OFF-001")
    .customerId("CUST-001")
    .customerSegment("PREMIUM")  // PREMIUM, GOLD, SILVER, REGULAR
    .orderAmount(new BigDecimal("1500"))
    .productCategory("ELECTRONICS")
    .isFirstTimeCustomer(false)
    .offerValidUntil(LocalDate.of(2026, 12, 31))
    .build();
```

#### Order
```java
Order order = Order.builder()
    .orderId("ORD-001")
    .customerId("CUST-001")
    .orderAmount(new BigDecimal("500"))
    .deliveryZone("LOCAL")  // LOCAL, REGIONAL, NATIONAL, INTERNATIONAL
    .itemCount(3)
    .isPeakHour(false)
    .productType("ELECTRONICS")
    .build();
```

#### Payment
```java
Payment payment = Payment.builder()
    .paymentId("PAY-001")
    .orderId("ORD-001")
    .amount(new BigDecimal("1000"))
    .paymentMethod("CREDIT_CARD")  // CREDIT_CARD, DEBIT_CARD, UPI, etc.
    .customerType("REGULAR")  // NEW, REGULAR, VIP
    .transactionCount(5)
    .build();
```

## 🔧 Configuration

### kmodule.xml

```xml
<kmodule xmlns="http://www.drools.org/xsd/kmodule">
    <kbase name="offerKBase" packages="io.shaama.rulesengine.rules.offer">
        <ksession name="offerKSession" type="stateless"/>
    </kbase>
    <kbase name="orderKBase" packages="io.shaama.rulesengine.rules.order">
        <ksession name="orderKSession" type="stateless"/>
    </kbase>
    <kbase name="paymentKBase" packages="io.shaama.rulesengine.rules.payment">
        <ksession name="paymentKSession" type="stateless"/>
    </kbase>
    <kbase name="allRulesKBase" packages="io.shaama.rulesengine.rules" default="true">
        <ksession name="allRulesKSession" type="stateless" default="true"/>
    </kbase>
</kmodule>
```

## 🛠️ Modifying Rules

### Edit Rules

1. Open rule file: `src/main/resources/rules/offer-rules.drl`
2. Modify rule logic (discount %, thresholds, conditions)
3. Test changes: `./gradlew test`
4. Build KJAR: `./gradlew clean build`
5. Publish: `./gradlew publishToMavenLocal` or `./gradlew publish`

### Example Rule Modification

**Before:**
```drl
rule "First Time Customer - 15% Discount"
    when
        $offer: Offer(isFirstTimeCustomer() == true, 
                     orderAmount.compareTo(new BigDecimal("500")) >= 0)
    then
        $offer.setDiscountPercentage(new BigDecimal("15"));
        $offer.setDiscountAmount($offer.getOrderAmount().multiply(new BigDecimal("0.15")));
end
```

**After (20% discount):**
```drl
rule "First Time Customer - 20% Discount"
    when
        $offer: Offer(isFirstTimeCustomer() == true, 
                     orderAmount.compareTo(new BigDecimal("500")) >= 0)
    then
        $offer.setDiscountPercentage(new BigDecimal("20"));
        $offer.setDiscountAmount($offer.getOrderAmount().multiply(new BigDecimal("0.20")));
end
```

### Hot Reload in Services

For hot-reloading rules in consuming services, see [OFFER_SERVICE_PROMPT.md](OFFER_SERVICE_PROMPT.md).

## 📝 Best Practices

### Rule Design
- ✅ Use **stateless sessions** for thread safety
- ✅ Avoid `update()` calls to prevent infinite loops
- ✅ Use **salience** to control rule execution order
- ✅ Make rules **mutually exclusive** when possible
- ✅ Add detailed **logging** in rule consequences

### Testing
- ✅ Test each rule in isolation
- ✅ Test rule interactions and conflicts
- ✅ Test edge cases and boundary conditions
- ✅ Keep tests fast (< 2 seconds total)

### Performance
- ✅ Use stateless sessions (no state overhead)
- ✅ Keep rule conditions simple
- ✅ Use indexed fields in LHS patterns
- ✅ Monitor rule execution time

## 🐛 Troubleshooting

### Common Issues

**Issue**: Tests taking too long (> 5 seconds)
- **Cause**: Infinite loops from `update()` calls
- **Fix**: Remove all `update()` statements from rules

**Issue**: Rules not firing
- **Cause**: Lombok boolean getter not recognized
- **Fix**: Use `isField()` instead of `field` in conditions

**Issue**: Multiple rules overwriting each other
- **Cause**: Non-exclusive rule conditions in stateless mode
- **Fix**: Add mutually exclusive conditions or adjust salience

**Issue**: KieContainer not found
- **Cause**: KJAR not in classpath
- **Fix**: Run `./gradlew publishToMavenLocal`
