package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/** 가게 공지 게시중단 관리 쓰기 인바운드 포트(admin). */
@AdminApp
public interface ShopNoticeManagementCommandUseCase {

    void hideNotice(ShopNoticeHideCommand command);

    void unhideNotice(ShopNoticeUnhideCommand command);
}
