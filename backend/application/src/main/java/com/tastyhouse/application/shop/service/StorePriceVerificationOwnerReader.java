package com.tastyhouse.application.shop.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;
import com.tastyhouse.application.product.port.out.write.StorePriceVerificationRepository;
import com.tastyhouse.application.shared.marker.CeoApp;

@Component
@CeoApp
public class StorePriceVerificationOwnerReader {

    private final StorePriceVerificationRepository storePriceVerificationRepository;
    private final StorePriceVerificationPort storePriceVerificationPort;

    public StorePriceVerificationOwnerReader(
        StorePriceVerificationRepository storePriceVerificationRepository,
        StorePriceVerificationPort storePriceVerificationPort
    ) {
        this.storePriceVerificationRepository = storePriceVerificationRepository;
        this.storePriceVerificationPort = storePriceVerificationPort;
    }

    public Optional<StorePriceVerification> readLatest(Long shopId) {
        return storePriceVerificationRepository.findLatestByShopId(ShopId.of(shopId));
    }

    public boolean readVerified(Long shopId) {
        return storePriceVerificationPort.isStorePriceVerified(shopId);
    }
}
