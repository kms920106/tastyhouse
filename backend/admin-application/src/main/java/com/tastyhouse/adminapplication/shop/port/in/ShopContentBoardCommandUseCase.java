package com.tastyhouse.adminapplication.shop.port.in;

/** 가게 콘텐츠보드 관리 쓰기 인바운드 포트(admin). */
public interface ShopContentBoardCommandUseCase {

    void changeHidden(ShopContentBoardHiddenChangeCommand command);

    void deleteContentBoard(ShopContentBoardDeleteCommand command);
}
