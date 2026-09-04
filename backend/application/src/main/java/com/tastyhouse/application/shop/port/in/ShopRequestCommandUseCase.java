package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 점주 가게 요청(취소·문의) 쓰기 인바운드 포트.
 */
@CeoApp
public interface ShopRequestCommandUseCase {

    void cancelRequest(ShopRequestCancelCommand command);

    Long addComment(ShopRequestCommentOwnerCreateCommand command);
}
