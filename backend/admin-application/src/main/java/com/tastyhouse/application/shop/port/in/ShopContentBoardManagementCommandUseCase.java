package com.tastyhouse.application.shop.port.in;

public interface ShopContentBoardManagementCommandUseCase {

    void changeHidden(ShopContentBoardHiddenChangeCommand command);

    void deleteContentBoard(ShopContentBoardManagementDeleteCommand command);
}
