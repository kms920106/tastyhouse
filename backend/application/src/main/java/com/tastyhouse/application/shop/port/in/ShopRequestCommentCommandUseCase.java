package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/** 요청건 답변 등록 쓰기 인바운드 포트(admin). */
@AdminApp
public interface ShopRequestCommentCommandUseCase {

    Long addComment(ShopRequestCommentManagementCreateCommand command);
}
