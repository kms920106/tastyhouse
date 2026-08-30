package com.tastyhouse.application.faq.port.out;

import java.util.List;

/**
 * FAQ 회원 노출 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>노출 상태의 분류·문항만 조회한다. 비노출을 포함한 관리 조회는
 * {@link FaqManagementQueryPort}가 소유한다 — 공유 메서드는 0개다.
 */
public interface FaqQueryPort {

    List<FaqCategoryResult> findVisibleCategories();

    List<FaqResult> findVisibleFaqs(Long categoryId);
}
