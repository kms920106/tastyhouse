package com.tastyhouse.core.domain.faq.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.faq.domain.model.FaqCategory;
import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;

/**
 * FAQ 카테고리 write 포트.
 *
 * <p>도메인 모델을 주고받는 CRUD와, 카테고리 삭제 불변식 검증에 필요한
 * {@link #existsActiveItemsByCategoryId}만 노출한다. 이 존재 검증은 화면 조립용이 아니라
 * {@code FaqCategoryDeletionPolicy}의 상태 전이 판단에 쓰이므로 write 포트에 잔류한다.
 * 목록·상세 등 표현 목적 read는 infrastructure-module의 {@code faq/query/FaqQueryDao}가 담당한다.
 */
public interface FaqCategoryRepository {

    Optional<FaqCategory> findById(FaqCategoryId faqCategoryId);

    /**
     * 해당 카테고리에 소속된 FAQ 항목이 남아 있는지 확인한다. 이름의 "Active"는 노출 여부가 아니라
     * 미삭제(deleted=false)를 뜻하므로, 비노출 항목만 남아 있어도 {@code true}다.
     */
    boolean existsActiveItemsByCategoryId(FaqCategoryId faqCategoryId);

    FaqCategory save(FaqCategory faqCategory);
}
