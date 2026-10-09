package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;

@Component
public class StorePriceVerificationOwnerReader {

    private final StorePriceVerificationPort storePriceVerificationPort;

    public StorePriceVerificationOwnerReader(StorePriceVerificationPort storePriceVerificationPort) {
        this.storePriceVerificationPort = storePriceVerificationPort;
    }

    public boolean readVerified(Long shopId) {
        return storePriceVerificationPort.isStorePriceVerified(shopId);
    }
}
