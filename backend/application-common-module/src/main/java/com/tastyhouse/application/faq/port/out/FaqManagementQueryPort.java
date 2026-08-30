package com.tastyhouse.application.faq.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * FAQ 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>비노출을 포함한 분류·문항의 관리 목록과 상세를 조회한다. 회원 노출 조회는
 * {@link FaqQueryPort}가 소유한다.
 */
public interface FaqManagementQueryPort {

    List<FaqCategoryManagementResult> findAllCategories();

    Optional<FaqCategoryManagementResult> findCategoryDetailById(Long categoryId);

    PageResult<FaqManagementListItemResult> findAllFaqs(FaqSearchCondition condition, PageQuery pageQuery);

    Optional<FaqDetailResult> findFaqDetailById(Long id);
}
