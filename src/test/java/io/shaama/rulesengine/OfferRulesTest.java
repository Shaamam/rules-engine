package io.shaama.rulesengine;

import io.shaama.rulesengine.model.Offer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OfferRulesTest {

    private static final Logger logger = LoggerFactory.getLogger(OfferRulesTest.class);
    private static StatelessKieSession kieSession;

    @BeforeAll
    static void setUp() {
        KieServices kieServices = KieServices.Factory.get();
        org.kie.api.io.Resource resource = kieServices.getResources()
                .newClassPathResource("rules/offer-rules.drl");
        org.kie.api.builder.KieFileSystem kfs = kieServices.newKieFileSystem();
        kfs.write(resource);
        org.kie.api.builder.KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
        kieBuilder.buildAll();
        org.kie.api.builder.KieModule kieModule = kieBuilder.getKieModule();
        KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        kieSession = kieContainer.newStatelessKieSession();
        kieSession.setGlobal("logger", logger);
    }    @Test
    void testFirstTimeCustomerOffer() {
        Offer offer = Offer.builder()
                .offerId("OFF-001")
                .customerId("CUST-001")
                .isFirstTimeCustomer(true)
                .orderAmount(new BigDecimal("600"))
                .offerApplicable(false)
                .build();

        kieSession.execute(offer);

        assertTrue(offer.isOfferApplicable());
        assertEquals(new BigDecimal("15"), offer.getDiscountPercentage());
        assertEquals(new BigDecimal("90.00"), offer.getDiscountAmount());
        assertEquals("FIRST_TIME_CUSTOMER", offer.getAppliedOfferType());
    }

    @Test
    void testPremiumCustomerOffer() {
        Offer offer = Offer.builder()
                .offerId("OFF-002")
                .customerId("CUST-002")
                .customerSegment("PREMIUM")
                .orderAmount(new BigDecimal("1500"))
                .isFirstTimeCustomer(false)
                .offerApplicable(false)
                .build();

        kieSession.execute(offer);

        assertTrue(offer.isOfferApplicable());
        assertEquals(new BigDecimal("20"), offer.getDiscountPercentage());
        assertEquals(new BigDecimal("300.00"), offer.getDiscountAmount());
        assertEquals("PREMIUM_CUSTOMER", offer.getAppliedOfferType());
    }

    @Test
    void testGoldCustomerOffer() {
        Offer offer = Offer.builder()
                .offerId("OFF-003")
                .customerId("CUST-003")
                .customerSegment("GOLD")
                .orderAmount(new BigDecimal("900"))
                .isFirstTimeCustomer(false)
                .offerApplicable(false)
                .build();

        kieSession.execute(offer);

        assertTrue(offer.isOfferApplicable());
        assertEquals(new BigDecimal("15"), offer.getDiscountPercentage());
        assertEquals("GOLD_CUSTOMER", offer.getAppliedOfferType());
    }

    @Test
    void testElectronicsCategoryOffer() {
        Offer offer = Offer.builder()
                .offerId("OFF-004")
                .customerId("CUST-004")
                .productCategory("ELECTRONICS")
                .orderAmount(new BigDecimal("2500"))
                .isFirstTimeCustomer(false)
                .offerApplicable(false)
                .build();

        kieSession.execute(offer);

        assertTrue(offer.isOfferApplicable());
        assertEquals(new BigDecimal("10"), offer.getDiscountPercentage());
        assertEquals("ELECTRONICS_CATEGORY", offer.getAppliedOfferType());
    }

    @Test
    void testFashionCategoryOffer() {
        Offer offer = Offer.builder()
                .offerId("OFF-005")
                .customerId("CUST-005")
                .productCategory("FASHION")
                .orderAmount(new BigDecimal("2000"))
                .isFirstTimeCustomer(false)
                .offerApplicable(false)
                .build();

        kieSession.execute(offer);

        assertTrue(offer.isOfferApplicable());
        assertEquals(new BigDecimal("25"), offer.getDiscountPercentage());
        assertEquals("FASHION_CATEGORY", offer.getAppliedOfferType());
    }

    @Test
    void testExpiredOffer() {
        Offer offer = Offer.builder()
                .offerId("OFF-006")
                .customerId("CUST-006")
                .orderAmount(new BigDecimal("1000"))
                .offerValidUntil(LocalDate.now().minusDays(5))
                .isFirstTimeCustomer(false)
                .offerApplicable(false)
                .build();

        kieSession.execute(offer);

        assertFalse(offer.isOfferApplicable());
        assertEquals("Offer has expired", offer.getRejectionReason());
    }

    @Test
    void testBelowMinimumOrderAmount() {
        Offer offer = Offer.builder()
                .offerId("OFF-007")
                .customerId("CUST-007")
                .orderAmount(new BigDecimal("150"))
                .isFirstTimeCustomer(false)
                .offerApplicable(false)
                .build();

        kieSession.execute(offer);

        assertFalse(offer.isOfferApplicable());
        assertEquals("Order amount below minimum threshold of 200", offer.getRejectionReason());
    }

    @Test
    void testNoApplicableOffer() {
        Offer offer = Offer.builder()
                .offerId("OFF-008")
                .customerId("CUST-008")
                .customerSegment("REGULAR")
                .orderAmount(new BigDecimal("300"))
                .isFirstTimeCustomer(false)
                .offerApplicable(false)
                .build();

        kieSession.execute(offer);

        assertFalse(offer.isOfferApplicable());
        // No rejection reason set because no rejection rule matched
        assertNull(offer.getRejectionReason());
    }
}
