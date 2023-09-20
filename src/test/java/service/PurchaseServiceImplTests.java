package service;


import com.wex.purchasetransaction.dto.PurchaseRequest;
import com.wex.purchasetransaction.entity.Purchase;
import com.wex.purchasetransaction.repository.PurchaseRepository;
import com.wex.purchasetransaction.service.PurchaseService;
import com.wex.purchasetransaction.service.impl.PurchaseServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PurchaseServiceImplTests {
    @InjectMocks
    PurchaseService purchaseService = new PurchaseServiceImpl();

    @Mock
    PurchaseRepository purchaseRepository;

    @DisplayName("Save a Purchase - Success test")
    @Test
    void testSavePurchase() {
        PurchaseRequest purchase = PurchaseRequest
                .builder()
                .description("Transaction test 1")
                .transactionDate(Instant.now())
                .amount(new BigDecimal("200.48"))
                .build();

        Purchase purchaseSaved = purchaseService.placePurchase(purchase);

        verify(purchaseRepository, times(1)).save(purchaseSaved);

        assertNotNull(purchaseSaved);
    }



}
