package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 리뷰 목록의 탭 구분.
 *
 * <p>ceo 목록은 web 목록과 달리 {@code hidden} 필터를 끄지 않는다 — {@link #BLINDED} 탭에서 차단된
 * 리뷰를 봐야 하기 때문이다. 탭은 "무엇을 보여줄지"만 정하고 정렬은 {@link ReviewSortType}이 담당한다.
 *
 * <p>같은 이유로 사장님만보기({@code ownerOnly}) 리뷰도 점주 목록에는 <b>필터 없이 자동 포함</b>된다 —
 * 비공개는 고객에게만 적용되는 개념이고 점주는 실제 피드백을 온전히 봐야 하기 때문이다.
 * {@link #OWNER_ONLY}는 그중 비공개 리뷰만 좁혀 보는 탭이며, {@link #BLINDED}와 <b>직교</b>한다
 * (한 리뷰가 두 탭에 동시에 나타날 수 있다).
 */
public enum ReviewListTab {

    ALL,          // 전체
    UNANSWERED,   // 미답변 (사장님 답변이 없는 리뷰)
    BLINDED,      // 차단 (게시중단된 리뷰)
    OWNER_ONLY;   // 사장님만보기 (작성자가 비공개로 등록한 리뷰)

    public static ReviewListTab from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.REVIEW_TAB_UNKNOWN,
                ErrorCode.REVIEW_TAB_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
