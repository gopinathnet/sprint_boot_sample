package com.trading.rfq.controller;

import com.trading.rfq.proto.CreateRfqRequest;
import com.trading.rfq.proto.RfqListResponse;
import com.trading.rfq.proto.RfqResponse;
import com.trading.rfq.service.RfqService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing RFQ operations.
 * 
 * Step 1 Concepts:
 * - @RestController combines @Controller and @ResponseBody (all methods return serialized data).
 * - Content Negotiation: produces = {"application/json", "application/x-protobuf"} allows
 *   callers to request either readable JSON or compact binary Protobuf bytes.
 * - Constructor Injection: We inject RfqService through the constructor (best practice in Spring).
 */
@RestController
@RequestMapping(value = "/api/v1/rfq", produces = {"application/json", "application/x-protobuf"})
public class RfqController {

    private final RfqService rfqService;

    public RfqController(RfqService rfqService) {
        this.rfqService = rfqService;
    }

    /**
     * Retrieve all RFQs.
     * Accessible via GET /api/v1/rfq
     */
    @GetMapping
    public ResponseEntity<RfqListResponse> getAllRfqs() {
        RfqListResponse response = rfqService.getAllRfqs();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve a single RFQ by ID.
     * Accessible via GET /api/v1/rfq/{id}
     * Returns 404 NOT FOUND if the RFQ does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RfqResponse> getRfqById(@PathVariable("id") String id) {
        return rfqService.getRfqById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Submit a new RFQ.
     * Accessible via POST /api/v1/rfq
     * Consumes both JSON and binary Protobuf payloads.
     */
    @PostMapping(consumes = {"application/json", "application/x-protobuf"})
    public ResponseEntity<RfqResponse> createRfq(@RequestBody CreateRfqRequest request) {
        RfqResponse created = rfqService.createRfq(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
