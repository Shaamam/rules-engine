package io.shaama.rulesengine;

import io.shaama.rulesengine.model.Order;
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

class OrderRulesTest {

    private static final Logger logger = LoggerFactory.getLogger(OrderRulesTest.class);
    private static StatelessKieSession kieSession;

    @BeforeAll
    static void setUp() {
        KieServices kieServices = KieServices.Factory.get();
        org.kie.api.io.Resource resource = kieServices.getResources()
                .newClassPathResource("rules/order-rules.drl");
        org.kie.api.builder.KieFileSystem kfs = kieServices.newKieFileSystem();
        kfs.write(resource);
        org.kie.api.builder.KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
        kieBuilder.buildAll();
        org.kie.api.builder.KieModule kieModule = kieBuilder.getKieModule();
        KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        kieSession = kieContainer.newStatelessKieSession();
        kieSession.setGlobal("logger", logger);
    }    @Test
    void testFreeShipping() {
        Order order = Order.builder()
                .orderId("ORD-001")
                .customerId("CUST-001")
                .orderAmount(new BigDecimal("1200"))
                .deliveryZone("REGIONAL")
                .orderDate(LocalDateTime.now())
                .itemCount(3)
                .isPeakHour(false)
                .freeShippingEligible(false)
                .build();

        kieSession.execute(order);

        assertTrue(order.isFreeShippingEligible());
        assertEquals(BigDecimal.ZERO, order.getShippingCharge());
    }

    @Test
    void testLocalShipping() {
        Order order = Order.builder()
                .orderId("ORD-002")
                .customerId("CUST-002")
                .orderAmount(new BigDecimal("500"))
                .deliveryZone("LOCAL")
                .orderDate(LocalDateTime.now())
                .itemCount(2)
                .isPeakHour(false)
                .freeShippingEligible(false)
                .build();

        kieSession.execute(order);

        assertFalse(order.isFreeShippingEligible());
        assertEquals(new BigDecimal("50"), order.getShippingCharge());
    }

    @Test
    void testRegionalShipping() {
        Order order = Order.builder()
                .orderId("ORD-003")
                .customerId("CUST-003")
                .orderAmount(new BigDecimal("800"))
                .deliveryZone("REGIONAL")
                .orderDate(LocalDateTime.now())
                .itemCount(4)
                .isPeakHour(false)
                .freeShippingEligible(false)
                .build();

        kieSession.execute(order);

        assertEquals(new BigDecimal("100"), order.getShippingCharge());
    }

    @Test
    void testNationalShipping() {
        Order order = Order.builder()
                .orderId("ORD-004")
                .customerId("CUST-004")
                .orderAmount(new BigDecimal("600"))
                .deliveryZone("NATIONAL")
                .orderDate(LocalDateTime.now())
                .itemCount(1)
                .isPeakHour(false)
                .freeShippingEligible(false)
                .build();

        kieSession.execute(order);

        assertEquals(new BigDecimal("200"), order.getShippingCharge());
    }

    @Test
    void testInternationalShipping() {
        Order order = Order.builder()
                .orderId("ORD-005")
                .customerId("CUST-005")
                .orderAmount(new BigDecimal("900"))
                .deliveryZone("INTERNATIONAL")
                .orderDate(LocalDateTime.now())
                .itemCount(2)
                .isPeakHour(false)
                .freeShippingEligible(false)
                .build();

        kieSession.execute(order);

        assertEquals(new BigDecimal("500"), order.getShippingCharge());
    }

    @Test
    void testPeakHourProcessingFee() {
        Order order = Order.builder()
                .orderId("ORD-006")
                .customerId("CUST-006")
                .orderAmount(new BigDecimal("700"))
                .deliveryZone("LOCAL")
                .orderDate(LocalDateTime.now())
                .itemCount(3)
                .isPeakHour(true)
                .freeShippingEligible(false)
                .build();

        kieSession.execute(order);

        assertEquals(new BigDecimal("25"), order.getProcessingFee());
    }

    @Test
    void testHighValueOrderRequiresApproval() {
        Order order = Order.builder()
                .orderId("ORD-007")
                .customerId("CUST-007")
                .orderAmount(new BigDecimal("15000"))
                .deliveryZone("NATIONAL")
                .orderDate(LocalDateTime.now())
                .itemCount(5)
                .isPeakHour(false)
                .freeShippingEligible(false)
                .build();

        kieSession.execute(order);

        assertTrue(order.isRequiresApproval());
        assertEquals("HIGH", order.getOrderPriority());
    }

    @Test
    void testElectronicsHighPriority() {
        Order order = Order.builder()
                .orderId("ORD-008")
                .customerId("CUST-008")
                .orderAmount(new BigDecimal("6000"))
                .deliveryZone("REGIONAL")
                .productType("ELECTRONICS")
                .orderDate(LocalDateTime.now())
                .itemCount(2)
                .isPeakHour(false)
                .freeShippingEligible(false)
                .build();

        kieSession.execute(order);

        assertEquals("HIGH", order.getOrderPriority());
    }

    @Test
    void testBulkOrderMediumPriority() {
        Order order = Order.builder()
                .orderId("ORD-009")
                .customerId("CUST-009")
                .orderAmount(new BigDecimal("800"))
                .deliveryZone("LOCAL")
                .orderDate(LocalDateTime.now())
                .itemCount(15)
                .isPeakHour(false)
                .freeShippingEligible(false)
                .build();

        kieSession.execute(order);

        assertEquals("MEDIUM", order.getOrderPriority());
    }

    @Test
    void testTotalAmountCalculation() {
        Order order = Order.builder()
                .orderId("ORD-010")
                .customerId("CUST-010")
                .orderAmount(new BigDecimal("500"))
                .deliveryZone("LOCAL")
                .orderDate(LocalDateTime.now())
                .itemCount(2)
                .isPeakHour(true)
                .freeShippingEligible(false)
                // Pre-set shipping and processing for stateless session testing
                .shippingCharge(new BigDecimal("50"))
                .processingFee(new BigDecimal("25"))
                .build();

        kieSession.execute(order);

        // 500 + 50 (local shipping) + 25 (peak hour fee) = 575
        assertEquals(new BigDecimal("575"), order.getTotalAmount());
    }

    @Test
    void testBelowMinimumOrder() {
        Order order = Order.builder()
                .orderId("ORD-011")
                .customerId("CUST-011")
                .orderAmount(new BigDecimal("30"))
                .deliveryZone("LOCAL")
                .orderDate(LocalDateTime.now())
                .itemCount(1)
                .isPeakHour(false)
                .freeShippingEligible(false)
                .build();

        kieSession.execute(order);

        assertEquals("Order amount must be at least 50", order.getValidationMessage());
    }
}
