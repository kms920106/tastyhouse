package com.tastyhouse.application.review.port.in;

public interface ReviewMemberCountQueryUseCase {

    long countVisibleReviewsByMemberId(Long memberId);
}
