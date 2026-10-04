package com.tastyhouse.application.shop.port.in;

public interface ShopImageChangeCommandUseCase {

    void approveImageChange(ShopImageChangeApproveCommand command);

    void rejectImageChange(ShopImageChangeRejectCommand command);
}
