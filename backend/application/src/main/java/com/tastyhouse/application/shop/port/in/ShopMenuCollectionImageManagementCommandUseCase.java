package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/** 메뉴판 이미지 검수 쓰기 인바운드 포트(admin). */
@AdminApp
public interface ShopMenuCollectionImageManagementCommandUseCase {

    void approveMenuCollectionImage(ShopMenuCollectionImageApproveCommand command);

    void rejectMenuCollectionImage(ShopMenuCollectionImageRejectCommand command);
}
