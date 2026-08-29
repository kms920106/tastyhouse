package com.tastyhouse.adminapi.review.application.port.in;

/** 리뷰 게시중단 요청 심사 쓰기 인바운드 포트(admin). */
public interface ReviewBlindRequestCommandUseCase {

    void approveBlindRequest(ReviewBlindRequestApproveCommand command);

    void rejectBlindRequest(ReviewBlindRequestRejectCommand command);
}
