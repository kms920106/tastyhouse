package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 리뷰 목록의 탭 구분.
 *
 * <p>ceo 목록은 web 목록과 달리 {@code hidden} 필터를 끄지 않는다 — {@link #BLINDED} 탭에서 차단된
 * 리뷰를 봐야 하기 때문이다. 탭은 "무엇을 보여줄지"만 정하고 정렬은 {@link ReviewSortType}이 담당한다.
 */
public enum ReviewListTab {

    ALL,          // 전체
    UNANSWERED,   // 미답변 (사장님 답변이 없는 리뷰)
    BLINDED;      // 차단 (게시중단된 리뷰)

    public static ReviewListTab from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.REVIEW_TAB_UNKNOWN,
                ErrorCode.REVIEW_TAB_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
