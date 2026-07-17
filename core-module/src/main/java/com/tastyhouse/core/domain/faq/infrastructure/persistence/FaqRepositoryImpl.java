package com.tastyhouse.core.domain.faq.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.faq.domain.model.Faq;
import com.tastyhouse.core.domain.faq.domain.repository.FaqRepository;
import com.tastyhouse.core.domain.faq.domain.vo.FaqId;
import com.tastyhouse.core.domain.faq.application.dto.FaqSearchCondition;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqListItemResult;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqResult;
import com.tastyhouse.core.domain.faq.application.dto.result.QFaqListItemResult;
import com.tastyhouse.core.domain.faq.application.dto.result.QFaqResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.faq.domain.model.QFaq.faq;

@Repository
@RequiredArgsConstructor
public class FaqRepositoryImpl implements FaqRepository {

    private final JPAQueryFactory queryFactory;
    private final FaqJpaRepository faqJpaRepository;

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
                .where(faq.deleted.isFalse(), faq.visible.isTrue())
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
                        faq.deleted.isFalse(),
                        faq.visible.isTrue(),
                        faq.faqCategoryId.eq(categoryId)
                )
                .orderBy(faq.sort.asc())
                .fetch();
    }

    @Override
    public PageResult<FaqListItemResult> findFaqPage(FaqSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
                .select(faq.id.count())
                .from(faq)
                .where(
                        categoryIdEq(condition.categoryId()),
                        questionContains(condition.question()),
                        visibleEq(condition.visible()),
                        faq.deleted.isFalse()
                )
                .fetchOne();

        List<FaqListItemResult> items = queryFactory
                .select(new QFaqListItemResult(
                        faq.id,
                        faq.faqCategoryId,
                        faq.question,
                        faq.sort,
                        faq.visible,
                        faq.createdAt
                ))
                .from(faq)
                .where(
                        categoryIdEq(condition.categoryId()),
                        questionContains(condition.question()),
                        visibleEq(condition.visible()),
                        faq.deleted.isFalse()
                )
                .orderBy(faq.faqCategoryId.asc(), faq.sort.asc())
                .offset((long) pageQuery.page() * pageQuery.size())
                .limit(pageQuery.size())
                .fetch();

        return PageResult.of(items, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<Faq> findById(FaqId faqId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(faq)
                .where(faq.id.eq(faqId.value()), faq.deleted.isFalse())
                .fetchOne());
    }

    @Override
    public Faq save(Faq faq) {
        return faqJpaRepository.save(faq);
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? faq.faqCategoryId.eq(categoryId) : null;
    }

    private BooleanExpression questionContains(String question) {
        return StringUtils.hasText(question) ? faq.question.containsIgnoreCase(question) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? faq.visible.eq(visible) : null;
    }
}
