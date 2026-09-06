package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.port.StorePriceVerificationPort;

@Component
@WebApp
public class StorePriceVerificationReader {

    private final StorePriceVerificationPort storePriceVerificationPort;

    public StorePriceVerificationReader(StorePriceVerificationPort storePriceVerificationPort) {
        this.storePriceVerificationPort = storePriceVerificationPort;
    }

    public boolean readVerified(Long shopId) {
        return storePriceVerificationPort.isStorePriceVerified(shopId);
    }
}
