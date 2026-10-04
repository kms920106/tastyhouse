package com.tastyhouse.application.shop.port.in;

public interface ShopOrderNoticeOwnerCommandUseCase {

    void upsertOrderNotice(ShopOrderNoticeUpsertCommand command);
}
