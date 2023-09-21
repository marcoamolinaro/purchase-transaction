package service;


import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.wex.purchasetransaction.service.impl.PurchaseServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Log4j2
@ExtendWith(MockitoExtension.class)
public class PurchaseServiceImplTests {
    @InjectMocks
    PurchaseService purchaseService = new PurchaseServiceImpl();

    @Mock
    PurchaseRepository purchaseRepository;

    private ApiConfig apiConfig;

    private PurchaseRequest purchaseRequest;

    private PurchaseResponse purchaseResponse;

    private RatesExchangeResponse ratesExchangeResponse;

    private ExchangeResponse exchangeResponse;

    @BeforeEach
    public void setup() {
        purchaseRequest = PurchaseRequest
                .builder()
                .description("Transaction test 1")
                .transactionDate(Instant.now())
                .amount(new BigDecimal("200.48"))
                .build();

        purchaseResponse = PurchaseResponse
                .builder()
                .id(1L)
                .description("Transaction test 1")
                .transactionDate(Instant.now())
                .amount(new BigDecimal("200.48"))
                .build();

        apiConfig = ApiConfig
                .builder()
                .baseUrl("https://api.fiscaldata.treasury.gov/services/api/fiscal_service")
                .endPoint("/v1/accounting/od/rates_of_exchange")
                .fields("?fields=exchange_rate,record_date&filter=country_currency_desc:eq:")
                .recordDate(",record_date:lte:")
                .sort("&sort=-record_date&page[size]=1")
                .build();
    }

    @DisplayName("Save a Purchase - Success test")
    @Test
    void testSavePurchase() {
        Purchase purchaseSaved = purchaseService.placePurchase(purchaseRequest);

        verify(purchaseRepository, times(1)).save(purchaseSaved);

        assertNotNull(purchaseSaved);
        assertEquals("Transaction test 1", purchaseSaved.getDescription());
    }

    @DisplayName("Get Exchance - Success test")
    @Test
    void testGetExchange() {
        String countryCurrencyDesc = "Brazil-Real";

        log.info("Purchase [" + purchaseResponse + "] + countryCurrencyDesc [" + countryCurrencyDesc + "]");

        String purchaseTransactionDate = purchaseResponse.getTransactionDate().toString().substring(0,10);

        ratesExchangeResponse =
                getExchangeRateByCurrencyCountryAndDate(countryCurrencyDesc,purchaseTransactionDate);

        Double exchangeRate = Double.parseDouble(ratesExchangeResponse.getExchange_rate());

        BigDecimal convertedAmount =
                Util.calculateExchange(exchangeRate, purchaseResponse.getAmount());

        String exchangeDate = ratesExchangeResponse.getRecord_date();

        log.info("exchangeDate " + exchangeDate);

        int numberOfMonths = Util.calculateNumberOfMonths(exchangeDate,
                purchaseResponse.getTransactionDate().toString().substring(0,10));

        if (numberOfMonths > Util.NUMBER_BETWEEN_MONTHS) {
            throw new CustomException("The purchase cannot be converted to the target currency",
                    "MONTHS_BETWEEN_DATES_GREATER_THAN_EXPECTED",
                    400);
        }

        exchangeResponse = ExchangeResponse
                .builder()
                .id(purchaseResponse.getId())
                .description(purchaseResponse.getDescription())
                .transactionDate(purchaseResponse.getTransactionDate())
                .originalAmount(purchaseResponse.getAmount())
                .exchangeRate(exchangeRate)
                .convertedAmount(convertedAmount)
                .build();

        log.info("Exchange [" + exchangeResponse + "]");

        assertEquals(new BigDecimal("973.93"), exchangeResponse.getConvertedAmount());
     }

    @DisplayName("Get Exchance - Fail test - currency conversion rate is not available within 6 months")
    @Test
    void testGetExchangeFailCurrencyRateMoreThanSixMonths() {
        String countryCurrencyDesc = "Brazil-Real";

        log.info("Purchase [" + purchaseResponse + "] + countryCurrencyDesc [" + countryCurrencyDesc + "]");

        String purchaseTransactionDate = "2024-05-01";

        ratesExchangeResponse =
                getExchangeRateByCurrencyCountryAndDate(countryCurrencyDesc,purchaseTransactionDate);

        Double exchangeRate = Double.parseDouble(ratesExchangeResponse.getExchange_rate());

        BigDecimal convertedAmount =
                Util.calculateExchange(exchangeRate, purchaseResponse.getAmount());

        String exchangeDate = ratesExchangeResponse.getRecord_date();

        log.info("exchangeDate " + exchangeDate);

        int numberOfMonths = Util.calculateNumberOfMonths(exchangeDate,
                purchaseTransactionDate);

        log.info("numberOfMonths " + numberOfMonths);

        assertTrue(numberOfMonths > 6);

    }

    @DisplayName("Get Exchance - Fail test - Country-Currency not found")
    @Test
    void testGetExchangeFail() {
        String countryCurrencyDesc = "Brazil-Peso";

        log.info("Purchase [" + purchaseResponse + "] + countryCurrencyDesc [" + countryCurrencyDesc + "]");

        String purchaseTransactionDate = purchaseResponse.getTransactionDate().toString().substring(0,10);

        try {
            ratesExchangeResponse =
                    getExchangeRateByCurrencyCountryAndDate(countryCurrencyDesc, purchaseTransactionDate);
        } catch (CustomException ce) {
            log.info(ce.getMessage());
        }

        assertNull(ratesExchangeResponse);
    }

    private RatesExchangeResponse getExchangeRateByCurrencyCountryAndDate(String currencyCountry, String recordDate) {

        String url = apiConfig.getBaseUrl() +
                apiConfig.getEndPoint() +
                apiConfig.getFields() +
                Util.CURRENCY_COUNTRY_PARAM +
                apiConfig.getRecordDate() +
                Util.RECORD_DATE_PARAM +
                apiConfig.getSort();

        log.info("URL [" + url + "]");

        Map<String, String> params = new HashMap<String, String>();

        RestTemplate restTemplate = new RestTemplate();

        params.put("currencyCountry", currencyCountry);
        params.put("recordDate", recordDate);

        String result = restTemplate.getForObject(url, String.class, params);

        result = result.substring(Util.INITIAL_JSON_CONTENT, result.indexOf(Util.FINAL_JSON_CONTENT));

        log.info("result [" + result + "]");

        if (result == null || result.isEmpty()) {
            throw new CustomException("Data not found for " + currencyCountry + " and date " + recordDate,
                    "DATA_NOT_FOUND", 404);
        }

        RatesExchangeResponse ratesExchangeResponse = null;

        try {
            ratesExchangeResponse = new ObjectMapper().readValue(result, RatesExchangeResponse.class);
        } catch (Exception e) {
            throw new CustomException("Error to parse response", "PARSE_ERROR", 400);
        }

        log.info("Exchange Rate " + ratesExchangeResponse.getExchange_rate());
        log.info("Exchange Date " + ratesExchangeResponse.getRecord_date());

        return ratesExchangeResponse;
    }
}
