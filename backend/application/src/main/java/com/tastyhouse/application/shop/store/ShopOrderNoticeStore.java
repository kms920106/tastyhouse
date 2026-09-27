package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticeStatePort;

public class ShopOrderNoticeStore implements ShopOrderNoticeRepository {
    private final ShopOrderNoticeStatePort shopOrderNoticeStatePort;

    public ShopOrderNoticeStore(ShopOrderNoticeStatePort shopOrderNoticeStatePort) {
        this.shopOrderNoticeStatePort = shopOrderNoticeStatePort;
    }

    @Override
    public ShopOrderNotice save(ShopOrderNotice shopOrderNotice) {
        return ShopOrderNoticeStateMapper.toDomain(shopOrderNoticeStatePort.save(ShopOrderNoticeStateMapper.toState(shopOrderNotice)));
    }

    @Override
    public Optional<ShopOrderNotice> findByShopId(ShopId shopId) {
        return shopOrderNoticeStatePort.findByShopId(shopId.value()).map(ShopOrderNoticeStateMapper::toDomain);
    }
}
