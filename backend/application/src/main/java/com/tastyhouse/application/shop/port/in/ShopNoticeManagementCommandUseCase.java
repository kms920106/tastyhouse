package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ShopNoticeManagementCommandUseCase {

    void hideNotice(ShopNoticeHideCommand command);

    void unhideNotice(ShopNoticeUnhideCommand command);
}
