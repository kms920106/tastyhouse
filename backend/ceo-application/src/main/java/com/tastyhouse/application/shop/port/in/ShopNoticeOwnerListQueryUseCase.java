package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopNoticeResult;

public interface ShopNoticeOwnerListQueryUseCase {

    List<ShopNoticeResult> getNotices(Long ceoId, Long shopId);
}
