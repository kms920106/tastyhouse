package com.tastyhouse.application.shop.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopMapMarkerQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopMapMarkerResult;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;

@Service
@Transactional(readOnly = true)
class ShopMapMarkerQueryService implements ShopMapMarkerQueryUseCase {

    private final ShopSearchQueryPort shopSearchQueryPort;

    public ShopMapMarkerQueryService(ShopSearchQueryPort shopSearchQueryPort) {
        this.shopSearchQueryPort = shopSearchQueryPort;
    }

    @Override
    public List<ShopMapMarkerResult> searchMapMarkers(Double latitude, Double longitude) {
        BigDecimal lat = BigDecimal.valueOf(latitude);
        BigDecimal lon = BigDecimal.valueOf(longitude);
        return shopSearchQueryPort.findNearbyShops(lat, lon);
    }
}
