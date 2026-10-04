package com.tastyhouse.application.shop.port.in;

public interface ShopOrderNoticeManagementCommandUseCase {

    void hideOrderNotice(ShopOrderNoticeHideCommand command);

    void unhideOrderNotice(ShopOrderNoticeUnhideCommand command);
}
