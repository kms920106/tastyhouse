package com.tastyhouse.core.domain.faq.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.faq.domain.model.FaqCategory;
import com.tastyhouse.core.domain.faq.domain.repository.FaqCategoryRepository;
import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;
import com.tastyhouse.core.domain.faq.application.dto.FaqCategoryAdminDto;
import com.tastyhouse.core.domain.faq.application.dto.FaqCategoryResult;
import com.tastyhouse.core.domain.faq.application.dto.QFaqCategoryAdminDto;
import com.tastyhouse.core.domain.faq.application.dto.QFaqCategoryResult;

import static com.tastyhouse.core.domain.faq.domain.model.QFaq.faq;
import static com.tastyhouse.core.domain.faq.domain.model.QFaqCategory.faqCategory;

@Repository
@RequiredArgsConstructor
public class FaqCategoryRepositoryImpl implements FaqCategoryRepository {

    private final JPAQueryFactory queryFactory;
    private final FaqCategoryJpaRepository faqCategoryJpaRepository;

    @Override
    public List<FaqCategoryResult> findAllActiveCategories() {
        return queryFactory
                .select(new QFaqCategoryResult(
                        faqCategory.id,
                        faqCategory.name,
                        faqCategory.sort
                ))
                .from(faqCategory)
                .where(faqCategory.deleted.isFalse(), faqCategory.visible.isTrue())
                .orderBy(faqCategory.sort.asc())
                .fetch();
    }

    @Override
    public List<FaqCategoryAdminDto> findAllCategories() {
        return queryFactory
                .select(new QFaqCategoryAdminDto(
                        faqCategory.id,
                        faqCategory.name,
                        faqCategory.sort,
                        faqCategory.visible,
                        faqCategory.createdAt
                ))
                .from(faqCategory)
                .where(faqCategory.deleted.isFalse())
                .orderBy(faqCategory.sort.asc())
                .fetch();
    }

    @Override
    public Optional<FaqCategory> findById(FaqCategoryId faqCategoryId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(faqCategory)
                .where(faqCategory.id.eq(faqCategoryId.value()), faqCategory.deleted.isFalse())
                .fetchOne());
    }

    @Override
    public boolean existsActiveItemsByCategoryId(FaqCategoryId faqCategoryId) {
        Integer result = queryFactory
                .selectOne()
                .from(faq)
                .where(faq.faqCategoryId.eq(faqCategoryId.value()), faq.deleted.isFalse())
                .fetchFirst();
        return result != null;
    }

    @Override
    public FaqCategory save(FaqCategory faqCategory) {
        return faqCategoryJpaRepository.save(faqCategory);
    }
}
