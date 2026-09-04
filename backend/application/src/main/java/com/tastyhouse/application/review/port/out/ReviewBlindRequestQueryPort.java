package com.tastyhouse.application.review.port.out;

import java.util.Optional;

/**
 * 리뷰 블라인드 안내 회원 노출 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>블라인드 처리된 리뷰 자리에 회원에게 보여줄 안내 문구만 조회한다. 블라인드 요청을 검수하는
 * 관리 화면 조회는 {@code ReviewBlindRequestManagementQueryPort}가 소유한다 — 공유 메서드는 0개다.
 */
public interface ReviewBlindRequestQueryPort {

    Optional<ReviewBlindNoticeResult> findBlindNotice(Long reviewId);
}
