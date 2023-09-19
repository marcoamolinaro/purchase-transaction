package com.wex.purchasetransaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatesExchangeResponse {
    private String exchange_rate;
    private String record_date;
}
