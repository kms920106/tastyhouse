package com.tastyhouse.application.shop.store;

import java.util.List;

import com.tastyhouse.domain.shop.model.ShopNoticeImage;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeImageStatePort;

public class ShopNoticeImageStore implements ShopNoticeImageRepository {
    private final ShopNoticeImageStatePort shopNoticeImageStatePort;

    public ShopNoticeImageStore(ShopNoticeImageStatePort shopNoticeImageStatePort) {
        this.shopNoticeImageStatePort = shopNoticeImageStatePort;
    }

    @Override
    public void saveAll(List<ShopNoticeImage> images) {
        shopNoticeImageStatePort.saveAll(images.stream().map(ShopNoticeImageStateMapper::toState).toList());
    }

    @Override
    public void deleteByShopNoticeId(Long shopNoticeId) {
        shopNoticeImageStatePort.deleteByShopNoticeId(shopNoticeId);
    }
}
