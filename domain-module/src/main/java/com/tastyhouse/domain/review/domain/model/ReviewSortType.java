package com.tastyhouse.domain.review.domain.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 리뷰 목록 정렬 방식 — 정렬 정책의 단일 원천.
 *
 * <p>과거에는 query DAO가 {@code "RECOMMENDED".equals(sortType)} 형태의 문자열 리터럴로 분기해
 * 오타 정렬값이 조용히 기본 정렬로 빠졌다. 승격은 도메인 enum 경계 규칙대로 소비 모듈의 Service가
 * {@link #from(String)}으로 수행하고, DAO는 이 enum만 받는다.
 */
public enum ReviewSortType {

    RECOMMENDED,  // 추천순 (좋아요 수 내림차순, 동수는 최신순)
    LATEST,       // 최신순 (기본)
    OLDEST;       // 등록순

    public static ReviewSortType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.REVIEW_SORT_TYPE_UNKNOWN,
                ErrorCode.REVIEW_SORT_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
