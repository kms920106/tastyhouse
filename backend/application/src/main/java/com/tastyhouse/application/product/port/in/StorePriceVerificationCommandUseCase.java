package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/** 매장 가격 인증 요청 검수 쓰기 인바운드 포트. */
@AdminApp
public interface StorePriceVerificationCommandUseCase {

    void startReview(StorePriceVerificationStartReviewCommand command);

    void approve(StorePriceVerificationApproveCommand command);

    void reject(StorePriceVerificationRejectCommand command);
}
