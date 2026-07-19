package com.tastyhouse.infrastructure.faq.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.faq.domain.model.FaqCategory;
import com.tastyhouse.core.domain.faq.domain.repository.FaqCategoryRepository;
import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqCategoryManagementResult;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqCategoryResult;
import com.tastyhouse.core.domain.faq.application.dto.result.QFaqCategoryManagementResult;
import com.tastyhouse.core.domain.faq.application.dto.result.QFaqCategoryResult;

import static com.tastyhouse.infrastructure.faq.persistence.QFaqCategoryJpaEntity.faqCategoryJpaEntity;
import static com.tastyhouse.infrastructure.faq.persistence.QFaqJpaEntity.faqJpaEntity;

@Repository
@RequiredArgsConstructor
public class FaqCategoryRepositoryImpl implements FaqCategoryRepository {

    private final JPAQueryFactory queryFactory;
    private final FaqCategoryJpaRepository faqCategoryJpaRepository;

    @Override
    public List<FaqCategoryResult> findAllActiveCategories() {
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

    @Override
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

    @Override
    public Optional<FaqCategory> findById(FaqCategoryId faqCategoryId) {
        FaqCategoryJpaEntity entity = queryFactory
                .selectFrom(faqCategoryJpaEntity)
                .where(faqCategoryJpaEntity.id.eq(faqCategoryId.value()), faqCategoryJpaEntity.deleted.isFalse())
                .fetchOne();
        return Optional.ofNullable(entity).map(FaqCategoryMapper::toDomain);
    }

    @Override
    public boolean existsActiveItemsByCategoryId(FaqCategoryId faqCategoryId) {
        Integer result = queryFactory
                .selectOne()
                .from(faqJpaEntity)
                .where(faqJpaEntity.faqCategoryId.eq(faqCategoryId.value()), faqJpaEntity.deleted.isFalse())
                .fetchFirst();
        return result != null;
    }

    @Override
    public FaqCategory save(FaqCategory faqCategory) {
        if (faqCategory.getId() == null) {
            FaqCategoryJpaEntity saved = faqCategoryJpaRepository.save(FaqCategoryMapper.toEntity(faqCategory));
            return FaqCategoryMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        FaqCategoryJpaEntity entity = faqCategoryJpaRepository.findById(faqCategory.getId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 FAQ 카테고리입니다: " + faqCategory.getId()));
        FaqCategoryMapper.applyChanges(entity, faqCategory);
        return FaqCategoryMapper.toDomain(entity);
    }
}
