package com.tastyhouse.application.shop.port.out;

import java.util.List;

public interface ShopNoticeOwnerQueryPort {

    List<ShopNoticeResult> findNotices(Long shopId);
}
