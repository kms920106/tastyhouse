package com.tastyhouse.adminapi.shop.application.port.in;

/** 메뉴판 이미지 검수 쓰기 인바운드 포트(admin). */
public interface ShopMenuCollectionImageCommandUseCase {

    void approveMenuCollectionImage(ShopMenuCollectionImageApproveCommand command);

    void rejectMenuCollectionImage(ShopMenuCollectionImageRejectCommand command);
}
