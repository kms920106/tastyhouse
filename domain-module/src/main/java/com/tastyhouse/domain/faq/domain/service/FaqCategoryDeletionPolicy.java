package com.tastyhouse.domain.faq.domain.service;

import com.tastyhouse.domain.faq.domain.model.FaqCategory;
import com.tastyhouse.domain.faq.domain.repository.FaqCategoryRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * FAQ 카테고리 삭제 규칙(도메인 서비스).
 *
 * <p>"소속된 FAQ 항목이 있으면 카테고리를 삭제할 수 없다"는 규칙은 카테고리 애그리거트 하나만으로
 * 판단할 수 없는 크로스 애그리거트 규칙이므로, 소비 모듈의 command 서비스가 아니라 도메인 계층에
 * 둔다. 특정 액터(admin)에 묶이지 않는 도메인 불변식이라 여러 모듈에서 재사용해도 규칙이 갈리지 않는다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 호출자의 트랜잭션 안에서 실행된다.
 */
public class FaqCategoryDeletionPolicy {

    private final FaqCategoryRepository faqCategoryRepository;

    public FaqCategoryDeletionPolicy(FaqCategoryRepository faqCategoryRepository) {
        this.faqCategoryRepository = faqCategoryRepository;
    }

    /**
     * 카테고리를 삭제 가능한지 검증하고, 가능하면 삭제 상태로 전이시킨다.
     *
     * <p>저장은 호출자(command 서비스)가 {@code FaqCategoryRepository#save}로 수행한다 — 도메인이
     * 프레임워크-프리라 더티 체킹이 없으므로 명시적 save가 필요하다.
     *
     * @throws BusinessException 소속된 FAQ 항목이 남아 있는 경우(비노출 항목도 포함)
     */
    public void delete(FaqCategory faqCategory) {
        if (faqCategoryRepository.existsActiveItemsByCategoryId(faqCategory.getFaqCategoryId())) {
            throw new BusinessException(ErrorCode.FAQ_CATEGORY_HAS_ITEMS);
        }

        faqCategory.delete();
    }
}
