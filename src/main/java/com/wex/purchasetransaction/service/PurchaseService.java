package com.wex.purchasetransaction.service;

import com.wex.purchasetransaction.dto.PurchaseRequest;
import com.wex.purchasetransaction.dto.PurchaseResponse;

public interface PurchaseService {
    long placePurchase(PurchaseRequest purchaserRequest);

    PurchaseResponse getPurchaseById(Long id);
}
