package com.wex.purchasetransaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequest {
    private String description;

    private Instant transactionDate;

    private BigDecimal amount;
}
