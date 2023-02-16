package com.company;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import static org.apache.commons.lang3.Validate.notNull;

@Data
@Builder
@Jacksonized
public class OrderCreated {
    private final Long orderId;
    private final String address;

    public OrderCreated(Long orderId, String address) {
        this.orderId = notNull(orderId, "Order id cannot be null");
        this.address = notNull(address, "Address cannot be null");
    }
}
