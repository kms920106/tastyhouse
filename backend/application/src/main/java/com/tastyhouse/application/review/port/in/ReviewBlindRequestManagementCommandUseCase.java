package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/** 리뷰 게시중단 요청 심사 쓰기 인바운드 포트(admin). */
@AdminApp
public interface ReviewBlindRequestManagementCommandUseCase {

    void approveBlindRequest(ReviewBlindRequestApproveCommand command);

    void rejectBlindRequest(ReviewBlindRequestRejectCommand command);
}
