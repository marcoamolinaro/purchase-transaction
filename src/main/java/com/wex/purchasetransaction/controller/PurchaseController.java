package com.wex.purchasetransaction.controller;

import com.wex.purchasetransaction.dto.ExchangeResponse;
import com.wex.purchasetransaction.dto.PurchaseRequest;
import com.wex.purchasetransaction.dto.PurchaseResponse;
import com.wex.purchasetransaction.service.impl.PurchaseServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/purchase")
@Log4j2
public class PurchaseController {

    @Autowired
    private PurchaseServiceImpl purchaseService;

    @PostMapping("/placePurchase")
    public ResponseEntity<Long> placePurchase(@RequestBody PurchaseRequest purchaseRequest) {
        long id = purchaseService.placePurchase(purchaseRequest);
        log.info("Purchase Id: {}", id);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<PurchaseResponse> getPurchaseById(@PathVariable Long id) {
        PurchaseResponse purchaseResponse = purchaseService.getPurchaseById(id);
        return new ResponseEntity<>(purchaseResponse, HttpStatus.OK);
    }

    @GetMapping("/exchange")
    public ResponseEntity<ExchangeResponse> getExchangeById(@PathVariable Long id,
                                                            @PathVariable String countryCurrencyDesc) {
        ExchangeResponse exchangeResponse = purchaseService.getExchangeById(id, countryCurrencyDesc);
        return new ResponseEntity<>(exchangeResponse, HttpStatus.OK);
    }
}
