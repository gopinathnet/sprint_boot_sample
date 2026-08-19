package com.trading.rfq.controller;

import com.google.protobuf.util.JsonFormat;
import com.trading.rfq.config.ProtobufConfig;
import com.trading.rfq.proto.CreateRfqRequest;
import com.trading.rfq.proto.RfqListResponse;
import com.trading.rfq.proto.RfqResponse;
import com.trading.rfq.proto.RfqStatus;
import com.trading.rfq.proto.Side;
import com.trading.rfq.service.RfqService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-level slice tests using @WebMvcTest.
 * 
 * Testing Concept:
 * - @WebMvcTest boots only the web layer (DispatcherServlet, Filters, Converters, Controller).
 * - @MockBean provides a mock for RfqService to isolate HTTP handling and Content Negotiation.
 * - Tests both JSON and binary Protobuf content negotiation.
 */
@WebMvcTest(RfqController.class)
@Import(ProtobufConfig.class)
class RfqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RfqService rfqService;

    @Test
    @DisplayName("GET /api/v1/rfq - Should return RFQ list as JSON")
    void shouldReturnRfqListAsJson() throws Exception {
        RfqResponse rfq = RfqResponse.newBuilder()
                .setId("RFQ-1001")
                .setClientId("GOLDMAN-SACHS")
                .setInstrumentId("EUR/USD")
                .setSide(Side.BUY)
                .setQuantity(1_000_000.0)
                .setStatus(RfqStatus.QUOTED)
                .build();

        RfqListResponse listResponse = RfqListResponse.newBuilder()
                .addRfqs(rfq)
                .build();

        given(rfqService.getAllRfqs()).willReturn(listResponse);

        mockMvc.perform(get("/api/v1/rfq")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.rfqs[0].id").value("RFQ-1001"))
                .andExpect(jsonPath("$.rfqs[0].clientId").value("GOLDMAN-SACHS"))
                .andExpect(jsonPath("$.rfqs[0].instrumentId").value("EUR/USD"))
                .andExpect(jsonPath("$.rfqs[0].side").value("BUY"))
                .andExpect(jsonPath("$.rfqs[0].status").value("QUOTED"));
    }

    @Test
    @DisplayName("GET /api/v1/rfq - Should return RFQ list as binary Protobuf")
    void shouldReturnRfqListAsProtobuf() throws Exception {
        RfqResponse rfq = RfqResponse.newBuilder()
                .setId("RFQ-1001")
                .setClientId("GOLDMAN-SACHS")
                .setInstrumentId("EUR/USD")
                .setSide(Side.BUY)
                .setQuantity(1_000_000.0)
                .setStatus(RfqStatus.QUOTED)
                .build();

        RfqListResponse listResponse = RfqListResponse.newBuilder()
                .addRfqs(rfq)
                .build();

        given(rfqService.getAllRfqs()).willReturn(listResponse);

        MvcResult result = mockMvc.perform(get("/api/v1/rfq")
                        .accept("application/x-protobuf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/x-protobuf"))
                .andReturn();

        // Deserialize binary bytes back into Protobuf object to assert correctness
        byte[] responseBytes = result.getResponse().getContentAsByteArray();
        RfqListResponse deserialized = RfqListResponse.parseFrom(responseBytes);

        assertThat(deserialized.getRfqsCount()).isEqualTo(1);
        assertThat(deserialized.getRfqs(0).getId()).isEqualTo("RFQ-1001");
        assertThat(deserialized.getRfqs(0).getClientId()).isEqualTo("GOLDMAN-SACHS");
    }

    @Test
    @DisplayName("GET /api/v1/rfq/{id} - Should return 200 OK when found")
    void shouldReturnRfqById() throws Exception {
        RfqResponse rfq = RfqResponse.newBuilder()
                .setId("RFQ-1001")
                .setClientId("GOLDMAN-SACHS")
                .setInstrumentId("EUR/USD")
                .setSide(Side.BUY)
                .setQuantity(5_000_000.0)
                .setStatus(RfqStatus.QUOTED)
                .build();

        given(rfqService.getRfqById("RFQ-1001")).willReturn(Optional.of(rfq));

        mockMvc.perform(get("/api/v1/rfq/RFQ-1001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("RFQ-1001"))
                .andExpect(jsonPath("$.clientId").value("GOLDMAN-SACHS"));
    }

    @Test
    @DisplayName("GET /api/v1/rfq/{id} - Should return 404 NOT FOUND when not present")
    void shouldReturn404ForMissingRfq() throws Exception {
        given(rfqService.getRfqById("UNKNOWN")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/rfq/UNKNOWN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/rfq - Should accept JSON and create RFQ (201 CREATED)")
    void shouldCreateRfqFromJson() throws Exception {
        CreateRfqRequest request = CreateRfqRequest.newBuilder()
                .setClientId("CITADEL")
                .setInstrumentId("GBP/USD")
                .setSide(Side.SELL)
                .setQuantity(3_000_000.0)
                .setCurrency("USD")
                .build();

        RfqResponse created = RfqResponse.newBuilder()
                .setId("RFQ-2001")
                .setClientId("CITADEL")
                .setInstrumentId("GBP/USD")
                .setSide(Side.SELL)
                .setQuantity(3_000_000.0)
                .setCurrency("USD")
                .setStatus(RfqStatus.PENDING)
                .build();

        given(rfqService.createRfq(any(CreateRfqRequest.class))).willReturn(created);

        String jsonPayload = JsonFormat.printer().print(request);

        mockMvc.perform(post("/api/v1/rfq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("RFQ-2001"))
                .andExpect(jsonPath("$.clientId").value("CITADEL"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
