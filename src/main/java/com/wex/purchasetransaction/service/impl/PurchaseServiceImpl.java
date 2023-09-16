package com.wex.purchasetransaction.service.impl;

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

import java.math.MathContext;

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
}
