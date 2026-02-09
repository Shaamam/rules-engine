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
public class Payment {
    private String paymentId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, WALLET, COD
    private String customerType; // NEW, REGULAR, VIP
    private Integer transactionCount; // Number of successful transactions in last 30 days
    private LocalDateTime paymentDate;
    private String currency;
    private BigDecimal creditLimit;
    
    // Calculated fields (set by rules)
    private boolean paymentApproved;
    private String paymentStatus; // APPROVED, PENDING, REJECTED, REQUIRES_VERIFICATION
    private BigDecimal transactionFee;
    private BigDecimal cashbackAmount;
    private String cashbackPercentage;
    private boolean requiresManualReview;
    private String rejectionReason;
    private Integer riskScore; // 0-100, higher means more risky
}
