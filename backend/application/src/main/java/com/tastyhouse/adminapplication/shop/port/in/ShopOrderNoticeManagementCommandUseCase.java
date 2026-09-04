package com.tastyhouse.adminapplication.shop.port.in;

/** 주문안내 게시중단 관리 쓰기 인바운드 포트(admin). */
public interface ShopOrderNoticeManagementCommandUseCase {

    void hideOrderNotice(ShopOrderNoticeHideCommand command);

    void unhideOrderNotice(ShopOrderNoticeUnhideCommand command);
}
