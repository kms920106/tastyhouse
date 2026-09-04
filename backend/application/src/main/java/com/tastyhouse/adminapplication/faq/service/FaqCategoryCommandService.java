package com.tastyhouse.adminapplication.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.adminapplication.faq.port.in.FaqCategoryCommandUseCase;
import com.tastyhouse.adminapplication.faq.port.in.FaqCategoryCreateCommand;
import com.tastyhouse.adminapplication.faq.port.in.FaqCategoryDeleteCommand;
import com.tastyhouse.adminapplication.faq.port.in.FaqCategoryUpdateCommand;
import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.repository.FaqCategoryRepository;
import com.tastyhouse.domain.faq.service.FaqCategoryDeletionPolicy;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
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
public class FaqCategoryCommandService implements FaqCategoryCommandUseCase {

    private final FaqCategoryRepository faqCategoryRepository;
    private final FaqCategoryDeletionPolicy faqCategoryDeletionPolicy;

    public FaqCategoryCommandService(FaqCategoryRepository faqCategoryRepository, FaqCategoryDeletionPolicy faqCategoryDeletionPolicy) {
        this.faqCategoryRepository = faqCategoryRepository;
        this.faqCategoryDeletionPolicy = faqCategoryDeletionPolicy;
    }

    @Override
    public Long createCategory(FaqCategoryCreateCommand command) {
        FaqCategory faqCategory = FaqCategory.of(command.name(), command.sort(), command.visible());
        FaqCategory saved = faqCategoryRepository.save(faqCategory);
        return saved.getFaqCategoryId().value();
    }

    @Override
    public void updateCategory(FaqCategoryUpdateCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        FaqCategory faqCategory = findCategoryOrThrow(faqCategoryId);

        faqCategory.update(command.name(), command.sort(), command.visible());
        faqCategoryRepository.save(faqCategory);
    }

    @Override
    public void deleteCategory(FaqCategoryDeleteCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        FaqCategory faqCategory = findCategoryOrThrow(faqCategoryId);

        faqCategoryDeletionPolicy.delete(faqCategory);
        faqCategoryRepository.save(faqCategory);
    }

    private FaqCategory findCategoryOrThrow(FaqCategoryId faqCategoryId) {
        return faqCategoryRepository.findById(faqCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
