package com.wex.purchasetransaction.Util;

import com.wex.purchasetransaction.exception.CustomException;

import java.time.LocalDate;
import java.time.Period;

public class Util {

    public final static int NUMBER_BETWEEN_MONTHS = 6;
    public final static int INITIAL_JSON_CONTENT = 9;
    public final static String FINAL_JSON_CONTENT = "]";
    public final static String CURRENCY_COUNTRY_PARAM = "{currencyCountry}";
    public final static String RECORD_DATE_PARAM = "{recordDate}";
    public static int calculateNumberOfMonths(String startDate, String endDate)  {
        int totalMonths = -1;

        try {
            LocalDate localExchangeDate = LocalDate.parse(startDate);
            LocalDate localTransactionDate = LocalDate.parse(endDate);

            Period period = Period.between(localExchangeDate, localTransactionDate);

            totalMonths = Math.abs(period.getMonths()) + 1;
        } catch (Exception e) {
            new CustomException("Error to calculate number of months between dates",
                    "RUNTIME_EXCEPTION", 500);
        }

        return totalMonths;
    }
}
