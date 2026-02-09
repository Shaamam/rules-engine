package io.shaama.rulesengine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Offer {
    private String offerId;
    private String offerCode;
    private String customerId;
    private String customerSegment; // PREMIUM, GOLD, SILVER, REGULAR
    private BigDecimal orderAmount;
    private LocalDate offerValidUntil;
    private String productCategory;
    private boolean isFirstTimeCustomer;
    
    // Calculated fields (set by rules)
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private String appliedOfferType;
    private boolean offerApplicable;
    private String rejectionReason;
}
