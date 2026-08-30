package com.tastyhouse.application.review.port.out;

import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 리뷰 블라인드 요청 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>점주가 올린 블라인드 요청을 검수하기 위한 목록·상세를 조회한다. 회원에게 노출되는 안내 문구
 * 조회는 {@link ReviewBlindRequestQueryPort}가 소유한다.
 */
public interface ReviewBlindRequestManagementQueryPort {

    PageResult<ReviewBlindRequestListItemResult> findBlindRequestPage(ReviewBlindRequestSearchCondition condition, PageQuery pageQuery);

    Optional<ReviewBlindRequestDetailResult> findBlindRequestDetail(Long id);
}
