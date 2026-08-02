package com.tastyhouse.infrastructure.faq.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

/**
 * FAQ 카테고리 관리 목록·상세 조회 결과.
 *
 * <p>비노출 카테고리를 포함해 조회하므로 노출 여부(visible)와 생성일시를 갖는다. web 노출용 형제인
 * {@link FaqCategoryResult}와 같은 패키지에 공존해 이름이 충돌하므로 관리 화면 용도를 나타내는
 * {@code Management} 한정어를 붙였다.
 */
public record FaqCategoryManagementResult(
    Long id,
    String name,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt
) {

    @QueryProjection
    public FaqCategoryManagementResult {
    }
}
