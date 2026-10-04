package com.tastyhouse.application.shop.port.in;

public interface ShopNoticeManagementCommandUseCase {

    void hideNotice(ShopNoticeHideCommand command);

    void unhideNotice(ShopNoticeUnhideCommand command);
}
