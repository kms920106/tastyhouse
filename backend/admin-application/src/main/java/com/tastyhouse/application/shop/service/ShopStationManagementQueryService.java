package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopStationManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopChoiceManagementQueryPort;
import com.tastyhouse.application.shop.port.out.StationResult;

@Service
@Transactional(readOnly = true)
class ShopStationManagementQueryService implements ShopStationManagementQueryUseCase {

    private final ShopChoiceManagementQueryPort shopChoiceManagementQueryPort;

    public ShopStationManagementQueryService(ShopChoiceManagementQueryPort shopChoiceManagementQueryPort) {
        this.shopChoiceManagementQueryPort = shopChoiceManagementQueryPort;
    }

    @Override
    public List<StationResult> getStations() {
        return shopChoiceManagementQueryPort.findAllStations();
    }
}
