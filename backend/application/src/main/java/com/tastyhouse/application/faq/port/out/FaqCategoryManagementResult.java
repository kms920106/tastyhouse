package com.tastyhouse.application.faq.port.out;

import java.time.LocalDateTime;

/**
 * FAQ 카테고리 관리 목록·상세 조회 결과.
 *
 * <p>비노출 카테고리를 포함해 조회하므로 노출 여부(visible)와 생성일시를 갖는다. web 노출용 형제인
 * {@code FaqCategoryResult}와 패키지 경로는 같으나 다른 모듈에 있으며, 역할을 구분하기 위해 관리 화면 용도를 나타내는
 * {@code Management} 한정어를 붙였다.
 */
public record FaqCategoryManagementResult(
    Long id,
    String name,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt
) {
}
