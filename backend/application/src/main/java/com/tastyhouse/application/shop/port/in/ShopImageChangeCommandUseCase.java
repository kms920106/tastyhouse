package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/** 가게 이미지 변경 요청 검수 쓰기 인바운드 포트(admin). */
@AdminApp
public interface ShopImageChangeCommandUseCase {

    void approveImageChange(ShopImageChangeApproveCommand command);

    void rejectImageChange(ShopImageChangeRejectCommand command);
}
