package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ShopContentBoardManagementCommandUseCase {

    void changeHidden(ShopContentBoardHiddenChangeCommand command);

    void deleteContentBoard(ShopContentBoardManagementDeleteCommand command);
}
