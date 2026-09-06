package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.repository.ShopNoticeRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopNoticeExposureService {
    private final ShopNoticeRepository shopNoticeRepository;

    public ShopNoticeExposureService(ShopNoticeRepository shopNoticeRepository) {
        this.shopNoticeRepository = shopNoticeRepository;
    }

    public void expose(ShopId shopId, ShopNotice target) {
        if (target.getId() == null) {
            throw new IllegalArgumentException("영속되지 않은 공지는 노출할 수 없습니다.");
        }

        shopNoticeRepository.findExposedByShopId(shopId)
            .filter(current -> !current.getId().equals(target.getId()))
            .ifPresent(current -> {
                current.unexpose();
                shopNoticeRepository.save(current);
            });
        target.expose();
        shopNoticeRepository.save(target);
    }

    public void unexpose(ShopNotice target) {
        target.unexpose();
        shopNoticeRepository.save(target);
    }
}
