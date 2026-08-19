package com.trading.rfq.service;

import com.trading.rfq.proto.CreateRfqRequest;
import com.trading.rfq.proto.RfqListResponse;
import com.trading.rfq.proto.RfqResponse;
import com.trading.rfq.proto.RfqStatus;
import com.trading.rfq.proto.Side;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RfqService business logic.
 * 
 * Testing Concept:
 * - Unit tests test pure domain logic in isolation.
 * - Fast execution: No Spring ApplicationContext or HTTP server needed.
 */
class RfqServiceTest {

    private RfqService rfqService;

    @BeforeEach
    void setUp() {
        rfqService = new RfqService();
    }

    @Test
    @DisplayName("Should return pre-seeded RFQs on startup")
    void shouldReturnInitialRfqs() {
        RfqListResponse response = rfqService.getAllRfqs();

        assertThat(response.getRfqsList())
                .isNotEmpty()
                .hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should retrieve existing RFQ by ID")
    void shouldGetRfqById() {
        Optional<RfqResponse> rfq = rfqService.getRfqById("RFQ-1001");

        assertThat(rfq).isPresent();
        assertThat(rfq.get().getClientId()).isEqualTo("GOLDMAN-SACHS");
        assertThat(rfq.get().getInstrumentId()).isEqualTo("EUR/USD");
        assertThat(rfq.get().getSide()).isEqualTo(Side.BUY);
        assertThat(rfq.get().getStatus()).isEqualTo(RfqStatus.QUOTED);
    }

    @Test
    @DisplayName("Should return empty Optional when RFQ ID does not exist")
    void shouldReturnEmptyForNonExistentRfq() {
        Optional<RfqResponse> rfq = rfqService.getRfqById("NON-EXISTENT-ID");

        assertThat(rfq).isEmpty();
    }

    @Test
    @DisplayName("Should create new RFQ in PENDING status")
    void shouldCreateNewRfq() {
        CreateRfqRequest request = CreateRfqRequest.newBuilder()
                .setClientId("MORGAN-STANLEY")
                .setInstrumentId("USD/JPY")
                .setSide(Side.BUY)
                .setQuantity(10_000_000.0)
                .setCurrency("JPY")
                .build();

        RfqResponse created = rfqService.createRfq(request);

        assertThat(created.getId()).startsWith("RFQ-");
        assertThat(created.getClientId()).isEqualTo("MORGAN-STANLEY");
        assertThat(created.getInstrumentId()).isEqualTo("USD/JPY");
        assertThat(created.getSide()).isEqualTo(Side.BUY);
        assertThat(created.getQuantity()).isEqualTo(10_000_000.0);
        assertThat(created.getStatus()).isEqualTo(RfqStatus.PENDING);
        assertThat(created.getCreatedAt()).isPositive();
        assertThat(created.getExpiresAt()).isGreaterThan(created.getCreatedAt());

        // Verify it was stored and is queryable
        Optional<RfqResponse> retrieved = rfqService.getRfqById(created.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get()).isEqualTo(created);
    }
}
