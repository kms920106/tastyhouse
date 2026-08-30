package com.tastyhouse.ceoapplication.shop.port.in;

/**
 * 점주 가게 요청(취소·문의) 쓰기 인바운드 포트.
 */
public interface ShopRequestCommandUseCase {

    void cancelRequest(ShopRequestCancelCommand command);

    Long addComment(ShopRequestCommentCreateCommand command);
}
