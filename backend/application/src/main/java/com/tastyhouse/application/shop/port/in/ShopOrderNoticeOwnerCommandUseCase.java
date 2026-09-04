package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 점주 가게 주문안내 쓰기 인바운드 포트.
 */
@CeoApp
public interface ShopOrderNoticeOwnerCommandUseCase {

    void upsertOrderNotice(ShopOrderNoticeUpsertCommand command);
}
