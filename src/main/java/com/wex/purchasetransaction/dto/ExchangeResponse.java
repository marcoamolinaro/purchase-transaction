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
public class ExchangeResponse {
    private Long id;
    private String description;
    private Instant transactionDate;
    private BigDecimal originalAmount;
    private Double exchangeRate;
    private BigDecimal convertedAmount;
}
