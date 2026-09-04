package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/** 가게 콘텐츠보드 관리 쓰기 인바운드 포트(admin). */
@AdminApp
public interface ShopContentBoardManagementCommandUseCase {

    void changeHidden(ShopContentBoardHiddenChangeCommand command);

    void deleteContentBoard(ShopContentBoardManagementDeleteCommand command);
}
