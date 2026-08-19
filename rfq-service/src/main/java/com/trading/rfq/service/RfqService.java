package com.trading.rfq.service;

import com.trading.rfq.proto.CreateRfqRequest;
import com.trading.rfq.proto.RfqListResponse;
import com.trading.rfq.proto.RfqResponse;
import com.trading.rfq.proto.RfqStatus;
import com.trading.rfq.proto.Side;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service handling RFQ business logic.
 * 
 * Step 1 Concept - Layered Architecture:
 * - In Spring Boot microservices, we keep business logic isolated in @Service components.
 * - For Step 1 (bare minimum), state is managed in a thread-safe in-memory store (ConcurrentHashMap).
 * - In Step 2, this will be upgraded to PostgreSQL + JPA persistence.
 */
@Service
public class RfqService {

    // Thread-safe in-memory storage for Step 1
    private final Map<String, RfqResponse> rfqStore = new ConcurrentHashMap<>();

    public RfqService() {
        // Seed with sample market quotes so endpoints return live data immediately
        initSampleData();
    }

    private void initSampleData() {
        long now = Instant.now().toEpochMilli();
        long expiry = now + (60 * 1000); // 60 seconds validity window

        RfqResponse sample1 = RfqResponse.newBuilder()
                .setId("RFQ-1001")
                .setClientId("GOLDMAN-SACHS")
                .setInstrumentId("EUR/USD")
                .setSide(Side.BUY)
                .setQuantity(5_000_000.0)
                .setCurrency("USD")
                .setStatus(RfqStatus.QUOTED)
                .setPrice(1.08540)
                .setCreatedAt(now)
                .setExpiresAt(expiry)
                .build();

        RfqResponse sample2 = RfqResponse.newBuilder()
                .setId("RFQ-1002")
                .setClientId("CITADEL-SEC")
                .setInstrumentId("GBP/USD")
                .setSide(Side.SELL)
                .setQuantity(2_500_000.0)
                .setCurrency("USD")
                .setStatus(RfqStatus.PENDING)
                .setPrice(0.0)
                .setCreatedAt(now)
                .setExpiresAt(expiry)
                .build();

        rfqStore.put(sample1.getId(), sample1);
        rfqStore.put(sample2.getId(), sample2);
    }

    /**
     * Retrieve all active RFQs.
     */
    public RfqListResponse getAllRfqs() {
        List<RfqResponse> list = List.copyOf(rfqStore.values());
        return RfqListResponse.newBuilder()
                .addAllRfqs(list)
                .build();
    }

    /**
     * Retrieve a specific RFQ by its ID.
     */
    public Optional<RfqResponse> getRfqById(String id) {
        return Optional.ofNullable(rfqStore.get(id));
    }

    /**
     * Submit and create a new RFQ.
     */
    public RfqResponse createRfq(CreateRfqRequest request) {
        long now = Instant.now().toEpochMilli();
        long expiry = now + (30 * 1000); // 30s quote expiry window

        String rfqId = "RFQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        RfqResponse rfq = RfqResponse.newBuilder()
                .setId(rfqId)
                .setClientId(request.getClientId())
                .setInstrumentId(request.getInstrumentId())
                .setSide(request.getSide())
                .setQuantity(request.getQuantity())
                .setCurrency(request.getCurrency())
                .setStatus(RfqStatus.PENDING)
                .setPrice(0.0)
                .setCreatedAt(now)
                .setExpiresAt(expiry)
                .build();

        rfqStore.put(rfqId, rfq);
        return rfq;
    }
}
