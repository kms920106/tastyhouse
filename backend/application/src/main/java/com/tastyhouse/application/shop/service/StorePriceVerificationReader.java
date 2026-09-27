package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;
import com.tastyhouse.application.shared.marker.WebApp;

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
