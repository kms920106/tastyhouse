package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeStatePort;

public class ShopNoticeStore implements ShopNoticeRepository {
    private final ShopNoticeStatePort shopNoticeStatePort;

    public ShopNoticeStore(ShopNoticeStatePort shopNoticeStatePort) {
        this.shopNoticeStatePort = shopNoticeStatePort;
    }

    @Override
    public ShopNotice save(ShopNotice shopNotice) {
        return ShopNoticeStateMapper.toDomain(shopNoticeStatePort.save(ShopNoticeStateMapper.toState(shopNotice)));
    }

    @Override
    public Optional<ShopNotice> findById(Long id) {
        return shopNoticeStatePort.findById(id).map(ShopNoticeStateMapper::toDomain);
    }

    @Override
    public Optional<ShopNotice> findExposedByShopId(ShopId shopId) {
        return shopNoticeStatePort.findExposedByShopId(shopId.value()).map(ShopNoticeStateMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shopNoticeStatePort.deleteById(id);
    }
}
