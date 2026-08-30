package com.tastyhouse.adminapplication.shop.port.in;

/** 가게 이미지 변경 요청 검수 쓰기 인바운드 포트(admin). */
public interface ShopImageChangeCommandUseCase {

    void approveImageChange(ShopImageChangeApproveCommand command);

    void rejectImageChange(ShopImageChangeRejectCommand command);
}
