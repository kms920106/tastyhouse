package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/** 주문안내 게시중단 관리 쓰기 인바운드 포트(admin). */
@AdminApp
public interface ShopOrderNoticeManagementCommandUseCase {

    void hideOrderNotice(ShopOrderNoticeHideCommand command);

    void unhideOrderNotice(ShopOrderNoticeUnhideCommand command);
}
