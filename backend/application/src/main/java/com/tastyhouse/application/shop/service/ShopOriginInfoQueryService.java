package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.in.ShopOriginInfoQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class ShopOriginInfoQueryService implements ShopOriginInfoQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopOriginInfoQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public ShopOriginInfoResult getOriginInfo(Long shopId) {
        return shopBasicInfoQueryPort.findOriginInfo(shopId).orElse(null);
    }
}
