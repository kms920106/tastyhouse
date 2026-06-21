package com.tastyhouse.core.domain.faq.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.faq.application.dto.FaqCategoryResult;
import com.tastyhouse.core.domain.faq.application.dto.FaqResult;
import com.tastyhouse.core.domain.faq.application.dto.QFaqCategoryResult;
import com.tastyhouse.core.domain.faq.application.dto.QFaqResult;
import com.tastyhouse.core.domain.faq.domain.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.domain.faq.domain.model.QFaq.faq;
import static com.tastyhouse.core.domain.faq.domain.model.QFaqCategory.faqCategory;

@Repository
@RequiredArgsConstructor
public class FaqRepositoryImpl implements FaqRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FaqCategoryResult> findAllActiveCategories() {
        return queryFactory
                .select(new QFaqCategoryResult(
                        faqCategory.id,
                        faqCategory.name,
                        faqCategory.sort
                ))
                .from(faqCategory)
                .where(faqCategory.visible.isTrue())
                .orderBy(faqCategory.sort.asc())
                .fetch();
    }

    @Override
    public List<FaqResult> findAllActiveItems() {
        return queryFactory
                .select(new QFaqResult(
                        faq.id,
                        faq.faqCategoryId,
                        faq.question,
                        faq.answer,
                        faq.sort
                ))
                .from(faq)
                .where(faq.visible.isTrue())
                .orderBy(faq.faqCategoryId.asc(), faq.sort.asc())
                .fetch();
    }

    @Override
    public List<FaqResult> findActiveItemsByCategoryId(Long categoryId) {
        return queryFactory
                .select(new QFaqResult(
                        faq.id,
                        faq.faqCategoryId,
                        faq.question,
                        faq.answer,
                        faq.sort
                ))
                .from(faq)
                .where(
                        faq.visible.isTrue(),
                        faq.faqCategoryId.eq(categoryId)
                )
                .orderBy(faq.sort.asc())
                .fetch();
    }
}
