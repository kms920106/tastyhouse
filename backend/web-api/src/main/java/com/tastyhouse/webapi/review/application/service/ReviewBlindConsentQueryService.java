package com.tastyhouse.webapi.review.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.infrastructure.review.query.ReviewBlindNoticeResult;
import com.tastyhouse.infrastructure.review.query.ReviewBlindRequestQueryDao;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewBlindNoticeResponse;
import com.tastyhouse.webapi.review.application.port.in.ReviewBlindConsentQueryUseCase;

/**
 * 게시중단 리뷰 안내 조회 서비스(CQRS query 측).
 *
 * <p>동의·거부 화면이 "무엇이 왜 중단됐고 언제 다시 노출되는지"를 보여주려면 리뷰 본문·사유·재노출
 * 예정일이 필요한데, <b>일반 리뷰 상세 조회는 {@code hidden.isFalse()} 필터에 걸려 게시중단 리뷰에
 * 404를 낸다</b>. 그 필터는 "게시중단은 정책 위반 제재"라는 판단이라 완화할 수 없으므로 이 전용 경로를 둔다.
 *
 * <p><b>인가가 핵심</b>: 경로의 {@code reviewId}만 믿으면 남의 게시중단 리뷰 내용을 열람할 수 있는
 * IDOR이 된다. 투영 결과의 작성자와 인증 주체가 일치하는지 재검증한다.
 *
 * <p>불일치·부재를 모두 <b>404({@code REVIEW_NOT_FOUND})</b>로 응답한다 — 대상이 이미 게시중단된
 * 비공개 리뷰이므로 존재 자체를 숨기는 쪽이 맞고, 이는 동의·거부 명령 경로
 * ({@code ReviewBlindRequestService#consentToDelete})와 같은 판단이다.
 *
 * <p>명령 동작은 {@link ReviewBlindConsentCommandService}로 분리했다(CQRS).
 */
@Service
@Transactional(readOnly = true)
public class ReviewBlindConsentQueryService implements ReviewBlindConsentQueryUseCase {

    private final ReviewBlindRequestQueryDao reviewBlindRequestQueryDao;

    public ReviewBlindConsentQueryService(ReviewBlindRequestQueryDao reviewBlindRequestQueryDao) {
        this.reviewBlindRequestQueryDao = reviewBlindRequestQueryDao;
    }

    /**
     * 게시중단된 내 리뷰의 안내를 조회한다.
     *
     * @throws ResourceNotFoundException 리뷰가 없거나, 게시중단 상태가 아니거나, 타인의 리뷰인 경우
     */
    @Override
    public ReviewBlindNoticeResponse getBlindNotice(Long reviewId, Long memberId) {
        ReviewBlindNoticeResult notice = reviewBlindRequestQueryDao.findBlindNotice(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (!notice.reviewMemberId().equals(memberId)) {
            throw new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND);
        }

        return toBlindNoticeResponse(notice);
    }

    private ReviewBlindNoticeResponse toBlindNoticeResponse(ReviewBlindNoticeResult result) {
        return ReviewBlindNoticeResponse.from(
            result.reviewId(),
            result.content(),
            result.imageUrls(),
            result.createdAt(),
            result.shopName(),
            result.reason().name(),
            result.reason().getDescription(),
            result.detailReason(),
            result.blindUntil()
        );
    }
}
