package com.tastyhouse.adminapplication.product.port.in;

/** 매장 가격 인증 요청 검수 쓰기 인바운드 포트. */
public interface StorePriceVerificationCommandUseCase {

    void startReview(StorePriceVerificationStartReviewCommand command);

    void approve(StorePriceVerificationApproveCommand command);

    void reject(StorePriceVerificationRejectCommand command);
}
