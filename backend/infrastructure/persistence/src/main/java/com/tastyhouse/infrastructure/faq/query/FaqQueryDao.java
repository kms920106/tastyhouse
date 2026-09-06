package com.tastyhouse.infrastructure.faq.query;

import com.tastyhouse.application.faq.port.out.FaqManagementQueryPort;
import com.tastyhouse.application.faq.port.out.FaqQueryPort;
import com.tastyhouse.application.faq.port.out.FaqCategoryManagementResult;
import com.tastyhouse.application.faq.port.out.FaqCategoryResult;
import com.tastyhouse.application.faq.port.out.FaqDetailResult;
import com.tastyhouse.application.faq.port.out.FaqManagementListItemResult;
import com.tastyhouse.application.faq.port.out.FaqResult;
import com.tastyhouse.application.faq.port.out.FaqSearchCondition;
import com.querydsl.core.types.Projections;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.faq.persistence.QFaqCategoryJpaEntity.faqCategoryJpaEntity;
import static com.tastyhouse.infrastructure.faq.persistence.QFaqJpaEntity.faqJpaEntity;

@Repository
public class FaqQueryDao implements FaqQueryPort, FaqManagementQueryPort {
    private final JPAQueryFactory queryFactory;

    public FaqQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<FaqCategoryResult> findVisibleCategories() {
        return queryFactory
            .select(Projections.constructor(FaqCategoryResult.class,
                faqCategoryJpaEntity.id,
                faqCategoryJpaEntity.name,
                faqCategoryJpaEntity.sort
            ))
            .from(faqCategoryJpaEntity)
            .where(faqCategoryJpaEntity.deleted.isFalse(), faqCategoryJpaEntity.visible.isTrue())
            .orderBy(faqCategoryJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<FaqCategoryManagementResult> findAllCategories() {
        return queryFactory
            .select(Projections.constructor(FaqCategoryManagementResult.class,
                faqCategoryJpaEntity.id,
                faqCategoryJpaEntity.name,
                faqCategoryJpaEntity.sort,
                faqCategoryJpaEntity.visible,
                faqCategoryJpaEntity.createdAt
            ))
            .from(faqCategoryJpaEntity)
            .where(faqCategoryJpaEntity.deleted.isFalse())
            .orderBy(faqCategoryJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public Optional<FaqCategoryManagementResult> findCategoryDetailById(Long categoryId) {
        if (categoryId == null) {
            return Optional.empty();
        }

        FaqCategoryManagementResult detail = queryFactory
            .select(Projections.constructor(FaqCategoryManagementResult.class,
                faqCategoryJpaEntity.id,
                faqCategoryJpaEntity.name,
                faqCategoryJpaEntity.sort,
                faqCategoryJpaEntity.visible,
                faqCategoryJpaEntity.createdAt
            ))
            .from(faqCategoryJpaEntity)
            .where(faqCategoryJpaEntity.id.eq(categoryId), faqCategoryJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(detail);
    }

    @Override
    public List<FaqResult> findVisibleFaqs(Long categoryId) {
        return queryFactory
            .select(Projections.constructor(FaqResult.class,
                faqJpaEntity.id,
                faqJpaEntity.faqCategoryId,
                faqJpaEntity.question,
                faqJpaEntity.answer,
                faqJpaEntity.sort
            ))
            .from(faqJpaEntity)
            .where(
                faqJpaEntity.deleted.isFalse(),
                faqJpaEntity.visible.isTrue(),
                categoryIdEq(categoryId)
            )
            .orderBy(faqJpaEntity.faqCategoryId.asc(), faqJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public PageResult<FaqManagementListItemResult> findAllFaqs(FaqSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(faqJpaEntity.id.count())
            .from(faqJpaEntity)
            .where(
                categoryIdEq(condition.categoryId()),
                questionContains(condition.question()),
                visibleEq(condition.visible()),
                faqJpaEntity.deleted.isFalse()
            )
            .fetchOne();

        List<FaqManagementListItemResult> items = queryFactory
            .select(Projections.constructor(FaqManagementListItemResult.class,
                faqJpaEntity.id,
                faqJpaEntity.faqCategoryId,
                faqJpaEntity.question,
                faqJpaEntity.sort,
                faqJpaEntity.visible,
                faqJpaEntity.createdAt
            ))
            .from(faqJpaEntity)
            .where(
                categoryIdEq(condition.categoryId()),
                questionContains(condition.question()),
                visibleEq(condition.visible()),
                faqJpaEntity.deleted.isFalse()
            )
            .orderBy(faqJpaEntity.faqCategoryId.asc(), faqJpaEntity.sort.asc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(items, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<FaqDetailResult> findFaqDetailById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        FaqDetailResult detail = queryFactory
            .select(Projections.constructor(FaqDetailResult.class,
                faqJpaEntity.id,
                faqJpaEntity.faqCategoryId,
                faqJpaEntity.question,
                faqJpaEntity.answer,
                faqJpaEntity.sort,
                faqJpaEntity.visible,
                faqJpaEntity.createdAt,
                faqJpaEntity.updatedAt
            ))
            .from(faqJpaEntity)
            .where(faqJpaEntity.id.eq(id), faqJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(detail);
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? faqJpaEntity.faqCategoryId.eq(categoryId) : null;
    }

    private BooleanExpression questionContains(String question) {
        return StringUtils.hasText(question) ? faqJpaEntity.question.containsIgnoreCase(question) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? faqJpaEntity.visible.eq(visible) : null;
    }
}
