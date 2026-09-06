package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.product.port.out.ProductShopLinkQueryPort;

@Component
@CeoApp
public class OwnedShopIdProvider {

    private final ProductShopLinkQueryPort productShopLinkQueryPort;

    public OwnedShopIdProvider(ProductShopLinkQueryPort productShopLinkQueryPort) {
        this.productShopLinkQueryPort = productShopLinkQueryPort;
    }

    public Set<Long> findOwnedShopIds(Long ceoId) {
        return new HashSet<>(productShopLinkQueryPort.findOwnedShopIds(ceoId));
    }
}
