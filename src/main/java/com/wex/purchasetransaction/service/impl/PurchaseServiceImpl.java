package com.wex.purchasetransaction.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.wex.purchasetransaction.Util.ApiConfig;
import com.wex.purchasetransaction.Util.Util;
import com.wex.purchasetransaction.dto.ExchangeResponse;
import com.wex.purchasetransaction.dto.PurchaseRequest;
import com.wex.purchasetransaction.dto.PurchaseResponse;
import com.wex.purchasetransaction.dto.RatesExchangeResponse;
import com.wex.purchasetransaction.entity.Purchase;
import com.wex.purchasetransaction.exception.CustomException;
import com.wex.purchasetransaction.repository.PurchaseRepository;
import com.wex.purchasetransaction.service.PurchaseService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ApiConfig apiConfig;
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

        String purchaseTransactionDate = purchaseResponse.getTransactionDate().toString().substring(0,10);

        // Find the exchange rate used for the specified Country-Currency
        RatesExchangeResponse ratesExchangeResponse =
                getExchangeRateByCurrencyCountryAndDate(countryCurrencyDesc,purchaseTransactionDate);

        Double exchangeRate = Double.parseDouble(ratesExchangeResponse.getExchange_rate());

        BigDecimal convertedAmount =
                calculateExchange(exchangeRate, purchaseResponse.getAmount());

        String exchangeDate = ratesExchangeResponse.getRecord_date();

        log.info("exchangeDate " + exchangeDate);

        int numberOfMonths = Util.calculateNumberOfMonths(exchangeDate,
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

    private RatesExchangeResponse getExchangeRateByCurrencyCountryAndDate(String currencyCountry, String recordDate) {

        log.info("BaseUrl " + apiConfig.getBaseUrl());
        log.info("EndPoit " + apiConfig.getEndPoint());
        log.info("Fields " + apiConfig.getFields());
        log.info("Record Date " + apiConfig.getRecordDate());
        log.info("Sort " + apiConfig.getSort());

        String url = apiConfig.getBaseUrl() +
                apiConfig.getEndPoint() +
                apiConfig.getFields() +
                "{currencyCountry}" +
                apiConfig.getRecordDate() +
                "{recordDate}" +
                apiConfig.getSort();

        log.info("URL [" + url + "]");

        Map<String, String> params = new HashMap<String, String>();

        RestTemplate restTemplate = new RestTemplate();

        params.put("currencyCountry", currencyCountry);
        params.put("recordDate", recordDate);

        String result = restTemplate.getForObject(url, String.class, params);

        result = result.substring(9, result.indexOf("]"));

        log.info("result " + result);

        RatesExchangeResponse ratesExchangeResponse = null;

        try {
            ratesExchangeResponse = new ObjectMapper().readValue(result, RatesExchangeResponse.class);
        } catch (Exception e) {
            throw new CustomException("Erro to parse string", "PARSE_ERROR", 500);
        }

        log.info("Exchange Rate " + ratesExchangeResponse.getExchange_rate());
        log.info("Exchange Date " + ratesExchangeResponse.getRecord_date());

        return ratesExchangeResponse;
    }
}
