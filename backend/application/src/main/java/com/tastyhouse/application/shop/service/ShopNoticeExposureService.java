package com.tastyhouse.application.shop.service;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shop.port.out.write.ShopNoticePersistencePort;

@CeoApp
public class ShopNoticeExposureService {

    private final ShopNoticePersistencePort shopNoticePersistencePort;

    public ShopNoticeExposureService(ShopNoticePersistencePort shopNoticePersistencePort) {
        this.shopNoticePersistencePort = shopNoticePersistencePort;
    }

    public void expose(ShopId shopId, ShopNotice target) {
        if (target.getId() == null) {
            throw new IllegalArgumentException("영속되지 않은 공지는 노출할 수 없습니다.");
        }

        shopNoticePersistencePort.findExposedByShopId(shopId)
            .filter(current -> !current.getId().equals(target.getId()))
            .ifPresent(current -> {
                current.unexpose();
                shopNoticePersistencePort.save(current);
            });
        target.expose();
        shopNoticePersistencePort.save(target);
    }

    public void unexpose(ShopNotice target) {
        target.unexpose();
        shopNoticePersistencePort.save(target);
    }
}
