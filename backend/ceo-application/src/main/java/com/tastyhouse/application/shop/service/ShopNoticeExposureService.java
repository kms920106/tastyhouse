package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeSavePort;

@Service
public class ShopNoticeExposureService {

    private final ShopNoticeLoadPort shopNoticeLoadPort;
    private final ShopNoticeSavePort shopNoticeSavePort;

    public ShopNoticeExposureService(ShopNoticeLoadPort shopNoticeLoadPort, ShopNoticeSavePort shopNoticeSavePort) {
        this.shopNoticeLoadPort = shopNoticeLoadPort;
        this.shopNoticeSavePort = shopNoticeSavePort;
    }

    public void expose(ShopId shopId, ShopNotice target) {
        if (target.getId() == null) {
            throw new IllegalArgumentException("영속되지 않은 공지는 노출할 수 없습니다.");
        }

        shopNoticeLoadPort.findExposedByShopId(shopId)
            .filter(current -> !current.getId().equals(target.getId()))
            .ifPresent(current -> {
                current.unexpose();
                shopNoticeSavePort.save(current);
            });
        target.expose();
        shopNoticeSavePort.save(target);
    }

    public void unexpose(ShopNotice target) {
        target.unexpose();
        shopNoticeSavePort.save(target);
    }
}
