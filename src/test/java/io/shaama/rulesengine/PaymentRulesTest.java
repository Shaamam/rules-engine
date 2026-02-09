package io.shaama.rulesengine;

import io.shaama.rulesengine.model.Payment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRulesTest {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRulesTest.class);
    private static StatelessKieSession kieSession;

    @BeforeAll
    static void setUp() {
        KieServices kieServices = KieServices.Factory.get();
        org.kie.api.io.Resource resource = kieServices.getResources()
                .newClassPathResource("rules/payment-rules.drl");
        org.kie.api.builder.KieFileSystem kfs = kieServices.newKieFileSystem();
        kfs.write(resource);
        org.kie.api.builder.KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
        kieBuilder.buildAll();
        org.kie.api.builder.KieModule kieModule = kieBuilder.getKieModule();
        KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        kieSession = kieContainer.newStatelessKieSession();
        kieSession.setGlobal("logger", logger);
    }    @Test
    void testHighValuePaymentManualReview() {
        Payment payment = Payment.builder()
                .paymentId("PAY-001")
                .orderId("ORD-001")
                .customerId("CUST-001")
                .amount(new BigDecimal("60000"))
                .paymentMethod("CREDIT_CARD")
                .customerType("REGULAR")
                .transactionCount(10)
                .paymentDate(LocalDateTime.now())
                .build();

        kieSession.execute(payment);

        assertTrue(payment.isRequiresManualReview());
        assertEquals("REQUIRES_VERIFICATION", payment.getPaymentStatus());
        assertEquals(75, payment.getRiskScore());
    }

    @Test
    void testCreditCardTransactionFee() {
        Payment payment = Payment.builder()
                .paymentId("PAY-002")
                .orderId("ORD-002")
                .customerId("CUST-002")
                .amount(new BigDecimal("1000"))
                .paymentMethod("CREDIT_CARD")
                .customerType("REGULAR")
                .transactionCount(5)
                .paymentDate(LocalDateTime.now())
                .build();

        kieSession.execute(payment);

        assertEquals(new BigDecimal("25.000"), payment.getTransactionFee());
        assertEquals("APPROVED", payment.getPaymentStatus());
    }

    @Test
    void testDebitCardTransactionFee() {
        Payment payment = Payment.builder()
                .paymentId("PAY-003")
                .orderId("ORD-003")
                .customerId("CUST-003")
                .amount(new BigDecimal("2000"))
                .paymentMethod("DEBIT_CARD")
                .customerType("REGULAR")
                .transactionCount(8)
                .paymentDate(LocalDateTime.now())
                .build();

        kieSession.execute(payment);

        assertEquals(new BigDecimal("30.000"), payment.getTransactionFee());
    }

    @Test
    void testUpiZeroFee() {
        Payment payment = Payment.builder()
                .paymentId("PAY-004")
                .orderId("ORD-004")
                .customerId("CUST-004")
                .amount(new BigDecimal("1500"))
                .paymentMethod("UPI")
                .customerType("REGULAR")
                .transactionCount(3)
                .paymentDate(LocalDateTime.now())
                .build();

        kieSession.execute(payment);

        assertEquals(BigDecimal.ZERO, payment.getTransactionFee());
    }

    @Test
    void testVipCashback() {
        Payment payment = Payment.builder()
                .paymentId("PAY-005")
                .orderId("ORD-005")
                .customerId("CUST-005")
                .amount(new BigDecimal("5000"))
                .paymentMethod("CREDIT_CARD")
                .customerType("VIP")
                .transactionCount(20)
                .paymentDate(LocalDateTime.now())
                .build();

        kieSession.execute(payment);

        assertEquals(new BigDecimal("250.00"), payment.getCashbackAmount());
        assertEquals("5%", payment.getCashbackPercentage());
    }

    @Test
    void testRegularCustomerCashback() {
        Payment payment = Payment.builder()
                .paymentId("PAY-006")
                .orderId("ORD-006")
                .customerId("CUST-006")
                .amount(new BigDecimal("6000"))
                .paymentMethod("UPI")
                .customerType("REGULAR")
                .transactionCount(10)
                .paymentDate(LocalDateTime.now())
                .build();

        kieSession.execute(payment);

        assertEquals(new BigDecimal("120.00"), payment.getCashbackAmount());
        assertEquals("2%", payment.getCashbackPercentage());
    }

    @Test
    void testCreditLimitExceeded() {
        Payment payment = Payment.builder()
                .paymentId("PAY-007")
                .orderId("ORD-007")
                .customerId("CUST-007")
                .amount(new BigDecimal("15000"))
                .paymentMethod("CREDIT_CARD")
                .customerType("REGULAR")
                .creditLimit(new BigDecimal("10000"))
                .transactionCount(5)
                .paymentDate(LocalDateTime.now())
                .build();

        kieSession.execute(payment);

        assertFalse(payment.isPaymentApproved());
        assertEquals("REJECTED", payment.getPaymentStatus());
        assertEquals("Payment amount exceeds credit limit", payment.getRejectionReason());
        assertEquals(100, payment.getRiskScore());
    }

    @Test
    void testNewCustomerVerification() {
        Payment payment = Payment.builder()
                .paymentId("PAY-008")
                .orderId("ORD-008")
                .customerId("CUST-008")
                .amount(new BigDecimal("12000"))
                .paymentMethod("DEBIT_CARD")
                .customerType("NEW")
                .transactionCount(0)
                .paymentDate(LocalDateTime.now())
                .build();

        kieSession.execute(payment);

        assertTrue(payment.isRequiresManualReview());
        assertEquals("REQUIRES_VERIFICATION", payment.getPaymentStatus());
        assertEquals(60, payment.getRiskScore());
    }

    @Test
    void testCodLimitExceeded() {
        Payment payment = Payment.builder()
                .paymentId("PAY-009")
                .orderId("ORD-009")
                .customerId("CUST-009")
                .amount(new BigDecimal("6000"))
                .paymentMethod("COD")
                .customerType("REGULAR")
                .transactionCount(5)
                .paymentDate(LocalDateTime.now())
                .build();

        kieSession.execute(payment);

        assertFalse(payment.isPaymentApproved());
        assertEquals("REJECTED", payment.getPaymentStatus());
        assertEquals("COD not available for orders above 5000", payment.getRejectionReason());
    }

    @Test
    void testLowRiskAutoApprove() {
        Payment payment = Payment.builder()
                .paymentId("PAY-010")
                .orderId("ORD-010")
                .customerId("CUST-010")
                .amount(new BigDecimal("3000"))
                .paymentMethod("UPI")
                .customerType("REGULAR")
                .transactionCount(15)
                .paymentDate(LocalDateTime.now())
                .build();

        kieSession.execute(payment);

        assertTrue(payment.isPaymentApproved());
        assertEquals("APPROVED", payment.getPaymentStatus());
        assertEquals(10, payment.getRiskScore());
    }

    @Test
    void testWalletTransactionFee() {
        Payment payment = Payment.builder()
                .paymentId("PAY-011")
                .orderId("ORD-011")
                .customerId("CUST-011")
                .amount(new BigDecimal("2500"))
                .paymentMethod("WALLET")
                .customerType("REGULAR")
                .transactionCount(7)
                .paymentDate(LocalDateTime.now())
                .build();

        kieSession.execute(payment);

        assertEquals(new BigDecimal("25.00"), payment.getTransactionFee());
    }
}
