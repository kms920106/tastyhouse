package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopOrderNoticeOwnerCommandUseCase {

    void upsertOrderNotice(ShopOrderNoticeUpsertCommand command);
}
