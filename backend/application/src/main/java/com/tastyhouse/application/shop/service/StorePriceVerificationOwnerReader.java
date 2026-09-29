package com.tastyhouse.application.shop.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;
import com.tastyhouse.application.product.port.out.write.StorePriceVerificationPersistencePort;
import com.tastyhouse.application.shared.marker.CeoApp;

@Component
@CeoApp
public class StorePriceVerificationOwnerReader {

    private final StorePriceVerificationPersistencePort storePriceVerificationPersistencePort;
    private final StorePriceVerificationPort storePriceVerificationPort;

    public StorePriceVerificationOwnerReader(
        StorePriceVerificationPersistencePort storePriceVerificationPersistencePort,
        StorePriceVerificationPort storePriceVerificationPort
    ) {
        this.storePriceVerificationPersistencePort = storePriceVerificationPersistencePort;
        this.storePriceVerificationPort = storePriceVerificationPort;
    }

    public Optional<StorePriceVerification> readLatest(Long shopId) {
        return storePriceVerificationPersistencePort.findLatestByShopId(ShopId.of(shopId));
    }

    public boolean readVerified(Long shopId) {
        return storePriceVerificationPort.isStorePriceVerified(shopId);
    }
}
