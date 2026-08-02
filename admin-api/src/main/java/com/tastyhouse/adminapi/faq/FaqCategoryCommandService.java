package com.tastyhouse.adminapi.faq;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.faq.domain.model.FaqCategory;
import com.tastyhouse.domain.faq.domain.repository.FaqCategoryRepository;
import com.tastyhouse.domain.faq.domain.service.FaqCategoryDeletionPolicy;
import com.tastyhouse.domain.faq.domain.vo.FaqCategoryId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * FAQ 카테고리 관리 command 서비스.
 *
 * <p>domain write 포트({@link FaqCategoryRepository})와 도메인 서비스
 * ({@link FaqCategoryDeletionPolicy})만 주입해 생성·수정·삭제를 수행한다. 조회는
 * {@link FaqQueryService}가 담당하며, 이 서비스는 infra query DAO를 주입하지 않는다.
 *
 * <p>"소속된 FAQ 항목이 있으면 삭제 금지"는 카테고리 애그리거트만으로 판단할 수 없는 크로스
 * 애그리거트 규칙이라 도메인 서비스로 하강되어 있고, 이 서비스는 그것을 호출한 뒤 저장만 한다.
 * {@code FaqCategory}는 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 save를 호출한다.
 */
@Service
@Transactional
public class FaqCategoryCommandService {

    private final FaqCategoryRepository faqCategoryRepository;
    private final FaqCategoryDeletionPolicy faqCategoryDeletionPolicy;

    public FaqCategoryCommandService(FaqCategoryRepository faqCategoryRepository, FaqCategoryDeletionPolicy faqCategoryDeletionPolicy) {
        this.faqCategoryRepository = faqCategoryRepository;
        this.faqCategoryDeletionPolicy = faqCategoryDeletionPolicy;
    }

    public Long createCategory(String name, Integer sort, boolean visible) {
        FaqCategory faqCategory = FaqCategory.of(name, sort, visible);
        FaqCategory saved = faqCategoryRepository.save(faqCategory);
        return saved.getFaqCategoryId().value();
    }

    public void updateCategory(Long categoryId, String name, Integer sort, boolean visible) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(categoryId);
        FaqCategory faqCategory = findCategoryOrThrow(faqCategoryId);

        faqCategory.update(name, sort, visible);
        faqCategoryRepository.save(faqCategory);
    }

    public void deleteCategory(Long categoryId) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(categoryId);
        FaqCategory faqCategory = findCategoryOrThrow(faqCategoryId);

        faqCategoryDeletionPolicy.delete(faqCategory);
        faqCategoryRepository.save(faqCategory);
    }

    private FaqCategory findCategoryOrThrow(FaqCategoryId faqCategoryId) {
        return faqCategoryRepository.findById(faqCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
