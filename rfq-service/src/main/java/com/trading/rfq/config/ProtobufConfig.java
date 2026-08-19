package com.trading.rfq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;

/**
 * Configuration for Protocol Buffers (Protobuf) HTTP message conversion.
 * 
 * Concept - Content Negotiation in Spring Web:
 * When a client calls a REST endpoint returning a Protobuf Message object:
 * - If client sends 'Accept: application/x-protobuf', Spring uses ProtobufHttpMessageConverter
 *   to serialize the response as compact binary bytes (low-latency, high performance).
 * - If client sends 'Accept: application/json' or default, Spring uses ProtobufJsonFormatHttpMessageConverter
 *   to serialize the response as standard JSON.
 */
@Configuration
public class ProtobufConfig {

    /**
     * ProtobufHttpMessageConverter handles binary Protobuf payloads ('application/x-protobuf').
     */
    @Bean
    public ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufHttpMessageConverter();
    }

    /**
     * ProtobufJsonFormatHttpMessageConverter enables JSON serialization for Protobuf messages.
     * It uses Google's JsonFormat printer so Protobuf enums, timestamps, and field tags
     * serialize properly to and from JSON.
     */
    @Bean
    public ProtobufJsonFormatHttpMessageConverter protobufJsonFormatHttpMessageConverter() {
        return new ProtobufJsonFormatHttpMessageConverter();
    }
}
