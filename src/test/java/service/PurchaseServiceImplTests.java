package service;


import com.wex.purchasetransaction.dto.PurchaseRequest;
import com.wex.purchasetransaction.dto.PurchaseResponse;
import com.wex.purchasetransaction.entity.Purchase;
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

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Log4j2
@ExtendWith(MockitoExtension.class)
public class PurchaseServiceImplTests {
    @InjectMocks
    PurchaseService purchaseService = new PurchaseServiceImpl();

    @Mock
    PurchaseRepository purchaseRepository;

    private PurchaseRequest purchaseRequest;

    private Purchase purchase;

    @BeforeEach
    public void setup() {
        purchaseRequest = PurchaseRequest
                .builder()
                .description("Transaction test 1")
                .transactionDate(Instant.now())
                .amount(new BigDecimal("200.48"))
                .build();

        purchase = Purchase
                .builder()
                .id(1L)
                .description("Transaction test 1")
                .transactionDate(Instant.now())
                .amount(new BigDecimal("200.48"))
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
}
