package io.shaama.rulesengine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String customerId;
    private BigDecimal orderAmount;
    private String deliveryZone; // LOCAL, REGIONAL, NATIONAL, INTERNATIONAL
    private LocalDateTime orderDate;
    private Integer itemCount;
    private boolean isPeakHour;
    private String productType; // ELECTRONICS, GROCERIES, FASHION, BOOKS, OTHER
    
    // Calculated fields (set by rules)
    private BigDecimal shippingCharge;
    private BigDecimal processingFee;
    private BigDecimal totalAmount;
    private String orderPriority; // HIGH, MEDIUM, LOW
    private boolean requiresApproval;
    private boolean freeShippingEligible;
    private String validationMessage;
}
