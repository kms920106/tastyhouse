package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ShopMenuCollectionImageManagementCommandUseCase {

    void approveMenuCollectionImage(ShopMenuCollectionImageApproveCommand command);

    void rejectMenuCollectionImage(ShopMenuCollectionImageRejectCommand command);
}
