package com.wex.purchasetransaction.dto;

import com.wex.purchasetransaction.entity.Purchase;
import jakarta.persistence.ConstructorResult;
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
public class PurchaseResponse {
    private Long id;

    private String description;

    private Instant transactionDate;

    private BigDecimal amount;
}
