package com.tastyhouse.webapi.review;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.service.ReviewBlindRequestService;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * 게시중단된 리뷰의 삭제 동의·거부 명령 서비스(CQRS command 측).
 *
 * <p>작성자 본인 검증(IDOR 방어)과 게시중단 상태 검증은 모두 도메인 서비스
 * ({@link ReviewBlindRequestService})가 소유한다 — 같은 규칙이 동의·거부 두 경로에 필요하고, 삭제가
 * 리뷰 생애주기 서비스를 경유해야 사진·태그 정리가 함께 일어나기 때문이다.
 *
 * <p>동의는 요청 종결·리뷰 삭제·인덱스 동기화가 <b>한 트랜잭션</b>에서 일어나야 하므로 그 경계를 이
 * 서비스가 선언한다.
 */
@Service
@Transactional
public class ReviewBlindConsentCommandService {

    private final ReviewBlindRequestService reviewBlindRequestService;

    public ReviewBlindConsentCommandService(ReviewBlindRequestService reviewBlindRequestService) {
        this.reviewBlindRequestService = reviewBlindRequestService;
    }

    /**
     * 고객이 리뷰 삭제에 동의한다 — 리뷰가 삭제되고 요청이 {@code DELETED}로 종결된다.
     */
    public void consent(Long reviewId, Long memberId) {
        reviewBlindRequestService.consentToDelete(ReviewId.of(reviewId), MemberId.of(memberId));
    }

    /**
     * 고객이 리뷰 삭제를 거부한다 — 아무 전이도 일어나지 않고 30일 뒤 배치가 재노출한다.
     */
    public void reject(Long reviewId, Long memberId) {
        reviewBlindRequestService.rejectDeletion(ReviewId.of(reviewId), MemberId.of(memberId));
    }
}
