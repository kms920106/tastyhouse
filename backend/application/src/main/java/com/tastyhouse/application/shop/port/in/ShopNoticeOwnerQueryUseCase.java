package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopNoticeResult;

@CeoApp
public interface ShopNoticeOwnerQueryUseCase {

    List<ShopNoticeResult> getNotices(Long ceoId, Long shopId);

    List<String> validateNotice(Long ceoId, Long shopId, String content);
}
