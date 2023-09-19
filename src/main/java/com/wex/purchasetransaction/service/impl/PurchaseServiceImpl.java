package com.wex.purchasetransaction.service.impl;

import com.wex.purchasetransaction.Util.Util;
import com.wex.purchasetransaction.dto.ExchangeResponse;
import com.wex.purchasetransaction.dto.PurchaseRequest;
import com.wex.purchasetransaction.dto.PurchaseResponse;
import com.wex.purchasetransaction.entity.Purchase;
import com.wex.purchasetransaction.exception.CustomException;
import com.wex.purchasetransaction.repository.PurchaseRepository;
import com.wex.purchasetransaction.service.PurchaseService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;

@Service
@Log4j2
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;
    @Transactional
    @Override
    public long placePurchase(PurchaseRequest purchaserRequest) {
        log.info("Place a Purchase transaction");

        Purchase purchase = Purchase
                .builder()
                .description(purchaserRequest.getDescription())
                .transactionDate(purchaserRequest.getTransactionDate())
                .amount(purchaserRequest.getAmount())
                .build();

        purchaseRepository.save(purchase);

        return purchase.getId();
    }

    @Transactional(readOnly = true)
    @Override
    public PurchaseResponse getPurchaseById(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new CustomException("Purchase transaction not found for the order Id: " + id,
                "NOT_FOUND",
                404));

        PurchaseResponse purchaserResponse = PurchaseResponse
                .builder()
                .id(purchase.getId())
                .description(purchase.getDescription())
                .transactionDate(purchase.getTransactionDate())
                .amount(purchase.getAmount())
                .build();

        return purchaserResponse;
    }

    @Transactional(readOnly = true)
    @Override
    public ExchangeResponse getExchangeById(Long id, String countryCurrencyDesc) {
        PurchaseResponse purchaseResponse = getPurchaseById(id);

        log.info("Purchase [" + purchaseResponse + "] + countryCurrencyDesc [" + countryCurrencyDesc + "]");

        Double exchangeRate = 4.585;

        BigDecimal convertedAmount =
                calculateExchange(exchangeRate, purchaseResponse.getAmount());

        // Find the exchange rate used for the specified Country-Currency

        // Verify if the date of the rate is within the last 6 mounths
        String exchangeDate = "2023-01-30";

        log.info("exchangeDate " + exchangeDate);

        int numberOfMonths = -1;

        numberOfMonths = Util.calculateNumberOfMonths(exchangeDate,
                purchaseResponse.getTransactionDate().toString().substring(0,10));

        log.info("Total of Months " + numberOfMonths);

        if (numberOfMonths > Util.NUMBER_BETWEEN_MONTHS) {
            throw new CustomException("The purchase cannot be converted to the target currency",
                    "MONTHS_BETWEEN_DATES_GREATHER_THAN_EXPECTED",
                    500);
        }

        // Build the exchange response
        ExchangeResponse exchangeResponse = ExchangeResponse
                .builder()
                .id(purchaseResponse.getId())
                .description(purchaseResponse.getDescription())
                .transactionDate(purchaseResponse.getTransactionDate())
                .originalAmount(purchaseResponse.getAmount())
                .exchangeRate(exchangeRate)
                .convertedAmount(convertedAmount)
                .build();

        log.info("Exchange [" + exchangeResponse + "]");

        return exchangeResponse;
    }

    private BigDecimal calculateExchange(Double exchangeRate, BigDecimal originalAmount) {
        BigDecimal convertedAmount =
                BigDecimal.valueOf(exchangeRate*originalAmount.doubleValue())
                        .setScale(2, RoundingMode.HALF_UP);
        return convertedAmount;
    }
}
