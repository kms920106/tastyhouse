package com.tastyhouse.adminapplication.shop.port.in;

/** 요청건 답변 등록 쓰기 인바운드 포트(admin). */
public interface ShopRequestCommentCommandUseCase {

    Long addComment(ShopRequestCommentCreateCommand command);
}
