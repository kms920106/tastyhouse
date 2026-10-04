package com.tastyhouse.application.shop.port.in;

public interface ShopMenuCollectionImageManagementCommandUseCase {

    void approveMenuCollectionImage(ShopMenuCollectionImageApproveCommand command);

    void rejectMenuCollectionImage(ShopMenuCollectionImageRejectCommand command);
}
