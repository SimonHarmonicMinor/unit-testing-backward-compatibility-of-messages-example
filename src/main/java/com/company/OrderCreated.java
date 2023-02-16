package com.company;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.Validate.notNull;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCreated {
    private final Long orderId;
    @Deprecated(forRemoval = true)
    private final String address;
    private final Address newAddress;
    @JsonAnySetter
    @Singular("any")
    private final Map<String, Object> additionalProperties;

    @JsonAnyGetter
    Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public OrderCreated(Long orderId, String address, Address newAddress, Map<String, Object> additionalProperties) {
        this.orderId = notNull(orderId, "Order id cannot be null");
        this.address = notNull(address, "Address cannot be null");
        this.newAddress = requireNonNullElse(newAddress, Address.builder().build());
        this.additionalProperties = requireNonNullElse(additionalProperties, emptyMap());
    }

    @Builder
    @Data
    @Jacksonized
    public static class Address {
        @Builder.Default
        private final String city = "";
        @Builder.Default
        private final String country = "";
        @Builder.Default
        private final Integer postcode = null;
    }
}
