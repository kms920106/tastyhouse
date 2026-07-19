package com.tastyhouse.infrastructure.faq.persistence;

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

import static com.tastyhouse.infrastructure.faq.persistence.QFaqJpaEntity.faqJpaEntity;

@Repository
@RequiredArgsConstructor
public class FaqRepositoryImpl implements FaqRepository {

    private final JPAQueryFactory queryFactory;
    private final FaqJpaRepository faqJpaRepository;

    @Override
    public List<FaqResult> findAllActiveItems() {
        return queryFactory
                .select(new QFaqResult(
                        faqJpaEntity.id,
                        faqJpaEntity.faqCategoryId,
                        faqJpaEntity.question,
                        faqJpaEntity.answer,
                        faqJpaEntity.sort
                ))
                .from(faqJpaEntity)
                .where(faqJpaEntity.deleted.isFalse(), faqJpaEntity.visible.isTrue())
                .orderBy(faqJpaEntity.faqCategoryId.asc(), faqJpaEntity.sort.asc())
                .fetch();
    }

    @Override
    public List<FaqResult> findActiveItemsByCategoryId(Long categoryId) {
        return queryFactory
                .select(new QFaqResult(
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
                        faqJpaEntity.faqCategoryId.eq(categoryId)
                )
                .orderBy(faqJpaEntity.sort.asc())
                .fetch();
    }

    @Override
    public PageResult<FaqListItemResult> findFaqPage(FaqSearchCondition condition, PageQuery pageQuery) {
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

        List<FaqListItemResult> items = queryFactory
                .select(new QFaqListItemResult(
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
    public Optional<Faq> findById(FaqId faqId) {
        FaqJpaEntity entity = queryFactory
                .selectFrom(faqJpaEntity)
                .where(faqJpaEntity.id.eq(faqId.value()), faqJpaEntity.deleted.isFalse())
                .fetchOne();
        return Optional.ofNullable(entity).map(FaqMapper::toDomain);
    }

    @Override
    public Faq save(Faq faq) {
        if (faq.getId() == null) {
            FaqJpaEntity saved = faqJpaRepository.save(FaqMapper.toEntity(faq));
            return FaqMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        FaqJpaEntity entity = faqJpaRepository.findById(faq.getId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 FAQ입니다: " + faq.getId()));
        FaqMapper.applyChanges(entity, faq);
        return FaqMapper.toDomain(entity);
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
