package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopStationListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopChoiceQueryPort;
import com.tastyhouse.application.shop.port.out.StationResult;

@Service
@Transactional(readOnly = true)
class ShopStationListQueryService implements ShopStationListQueryUseCase {

    private final ShopChoiceQueryPort shopChoiceQueryPort;

    public ShopStationListQueryService(ShopChoiceQueryPort shopChoiceQueryPort) {
        this.shopChoiceQueryPort = shopChoiceQueryPort;
    }

    @Override
    public List<StationResult> searchAllStations() {
        return shopChoiceQueryPort.findAllStations();
    }
}
