package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ShopImageChangeCommandUseCase {

    void approveImageChange(ShopImageChangeApproveCommand command);

    void rejectImageChange(ShopImageChangeRejectCommand command);
}
