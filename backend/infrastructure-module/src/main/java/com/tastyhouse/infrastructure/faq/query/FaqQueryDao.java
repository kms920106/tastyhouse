package com.tastyhouse.infrastructure.faq.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.faq.persistence.QFaqCategoryJpaEntity.faqCategoryJpaEntity;
import static com.tastyhouse.infrastructure.faq.persistence.QFaqJpaEntity.faqJpaEntity;

/**
 * FAQ read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로
 * write 포트({@code FaqRepository}/{@code FaqCategoryRepository})와 역할이 겹치지 않는다. 소비
 * 모듈(web-api/admin-api)의 {@code FaqQueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api
 * 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 항목·카테고리 두 애그리거트의 조회와 소비자별 메서드를 이 한
 * 클래스에 둔다. 메서드명에는 admin 마커를 붙이지 않고 순수 동작명을 쓴다
 * ({@code findAllCategories}는 비노출 포함 전체, {@code findVisibleCategories}는 노출분만).
 */
@Repository
public class FaqQueryDao {

    private final JPAQueryFactory queryFactory;

    public FaqQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 회원 노출 카테고리 목록 조회 — 노출(visible=true) 카테고리만 정렬 순서대로 조회한다.
     */
    public List<FaqCategoryResult> findVisibleCategories() {
        return queryFactory
            .select(new QFaqCategoryResult(
                faqCategoryJpaEntity.id,
                faqCategoryJpaEntity.name,
                faqCategoryJpaEntity.sort
            ))
            .from(faqCategoryJpaEntity)
            .where(faqCategoryJpaEntity.deleted.isFalse(), faqCategoryJpaEntity.visible.isTrue())
            .orderBy(faqCategoryJpaEntity.sort.asc())
            .fetch();
    }

    /**
     * 관리 카테고리 목록 조회 — 비노출 카테고리도 포함한다.
     */
    public List<FaqCategoryManagementResult> findAllCategories() {
        return queryFactory
            .select(new QFaqCategoryManagementResult(
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

    /**
     * 관리 카테고리 상세 조회 — 비노출 카테고리도 조회된다.
     */
    public Optional<FaqCategoryManagementResult> findCategoryDetailById(Long categoryId) {
        if (categoryId == null) {
            return Optional.empty();
        }

        FaqCategoryManagementResult detail = queryFactory
            .select(new QFaqCategoryManagementResult(
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

    /**
     * 회원 노출 항목 목록 조회 — 노출(visible=true) 항목만 조회한다. categoryId가 null이면 전체
     * 카테고리를 대상으로 하고, 값이 있으면 그 카테고리로 한정한다.
     */
    public List<FaqResult> findVisibleFaqs(Long categoryId) {
        return queryFactory
            .select(new QFaqResult(
                faqJpaEntity.id,
                faqCategoryIdPath(),
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
            .orderBy(faqCategoryIdPath().asc(), faqJpaEntity.sort.asc())
            .fetch();
    }

    /**
     * 관리 항목 목록 조회 — 비노출 항목을 포함하며 categoryId·visible 필터와 question 부분일치를 적용한다.
     */
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
            .select(new QFaqManagementListItemResult(
                faqJpaEntity.id,
                faqCategoryIdPath(),
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
            .orderBy(faqCategoryIdPath().asc(), faqJpaEntity.sort.asc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(items, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    /**
     * 관리 항목 상세 조회 — 비노출 항목도 조회된다.
     */
    public Optional<FaqDetailResult> findFaqDetailById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        FaqDetailResult detail = queryFactory
            .select(new QFaqDetailResult(
                faqJpaEntity.id,
                faqCategoryIdPath(),
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

    /**
     * {@code @Convert} VO 컬럼인 {@code FAQ.faq_category_id}를 raw {@code Long}으로 비교·투영하기 위한 path.
     */
    private NumberPath<Long> faqCategoryIdPath() {
        return Expressions.numberPath(Long.class, faqJpaEntity, "faqCategoryId");
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? faqCategoryIdPath().eq(categoryId) : null;
    }

    private BooleanExpression questionContains(String question) {
        return StringUtils.hasText(question) ? faqJpaEntity.question.containsIgnoreCase(question) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? faqJpaEntity.visible.eq(visible) : null;
    }
}
