package com.tastyhouse.application.shop.port.out.write;

import java.util.List;

public interface ShopNoticeImageStatePort {
    void saveAll(List<ShopNoticeImageState> images);

    void deleteByShopNoticeId(Long shopNoticeId);
}
