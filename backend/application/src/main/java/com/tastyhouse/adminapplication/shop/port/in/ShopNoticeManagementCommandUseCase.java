package com.tastyhouse.adminapplication.shop.port.in;

/** 가게 공지 게시중단 관리 쓰기 인바운드 포트(admin). */
public interface ShopNoticeManagementCommandUseCase {

    void hideNotice(ShopNoticeHideCommand command);

    void unhideNotice(ShopNoticeUnhideCommand command);
}
