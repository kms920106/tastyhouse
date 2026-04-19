package com.tastyhouse.core.repository.faq;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.faq.dto.FaqCategoryDto;
import com.tastyhouse.core.entity.faq.dto.FaqItemDto;
import com.tastyhouse.core.entity.faq.dto.QFaqCategoryDto;
import com.tastyhouse.core.entity.faq.dto.QFaqItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.entity.faq.QFaq.faq;
import static com.tastyhouse.core.entity.faq.QFaqCategory.faqCategory;

@Repository
@RequiredArgsConstructor
public class FaqRepositoryImpl implements FaqRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FaqCategoryDto> findAllActiveCategories() {
        return queryFactory
                .select(new QFaqCategoryDto(
                        faqCategory.id,
                        faqCategory.name,
                        faqCategory.sort
                ))
                .from(faqCategory)
                .where(faqCategory.active.isTrue())
                .orderBy(faqCategory.sort.asc())
                .fetch();
    }

    @Override
    public List<FaqItemDto> findAllActiveItems() {
        return queryFactory
                .select(new QFaqItemDto(
                        faq.id,
                        faq.faqCategoryId,
                        faq.question,
                        faq.answer,
                        faq.sort
                ))
                .from(faq)
                .where(faq.active.isTrue())
                .orderBy(faq.faqCategoryId.asc(), faq.sort.asc())
                .fetch();
    }

    @Override
    public List<FaqItemDto> findActiveItemsByCategoryId(Long categoryId) {
        return queryFactory
                .select(new QFaqItemDto(
                        faq.id,
                        faq.faqCategoryId,
                        faq.question,
                        faq.answer,
                        faq.sort
                ))
                .from(faq)
                .where(
                        faq.active.isTrue(),
                        faq.faqCategoryId.eq(categoryId)
                )
                .orderBy(faq.sort.asc())
                .fetch();
    }
}
