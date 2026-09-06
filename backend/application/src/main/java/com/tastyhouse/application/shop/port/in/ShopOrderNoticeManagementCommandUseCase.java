package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ShopOrderNoticeManagementCommandUseCase {

    void hideOrderNotice(ShopOrderNoticeHideCommand command);

    void unhideOrderNotice(ShopOrderNoticeUnhideCommand command);
}
