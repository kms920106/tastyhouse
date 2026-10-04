package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;

@Component
public class StorePriceVerificationReader {

    private final StorePriceVerificationPort storePriceVerificationPort;

    public StorePriceVerificationReader(StorePriceVerificationPort storePriceVerificationPort) {
        this.storePriceVerificationPort = storePriceVerificationPort;
    }

    public boolean readVerified(Long shopId) {
        return storePriceVerificationPort.isStorePriceVerified(shopId);
    }
}
