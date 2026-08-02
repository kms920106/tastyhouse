package com.tastyhouse.infrastructure.faq.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.faq.domain.model.FaqCategory;
import com.tastyhouse.domain.faq.domain.repository.FaqCategoryRepository;
import com.tastyhouse.domain.faq.domain.vo.FaqCategoryId;

import static com.tastyhouse.infrastructure.faq.persistence.QFaqCategoryJpaEntity.faqCategoryJpaEntity;
import static com.tastyhouse.infrastructure.faq.persistence.QFaqJpaEntity.faqJpaEntity;

/**
 * FAQ 카테고리 write 어댑터.
 *
 * <p>도메인 모델 단건 로드·저장과, 카테고리 삭제 불변식 검증용 존재 조회만 담당한다. 목록·상세 등
 * 표현 목적 조회는 같은 모듈의 {@code faq/query/FaqQueryDao}로 분리되어 있다.
 */
@Repository
public class FaqCategoryRepositoryImpl implements FaqCategoryRepository {

    private final JPAQueryFactory queryFactory;
    private final FaqCategoryJpaRepository faqCategoryJpaRepository;

    public FaqCategoryRepositoryImpl(JPAQueryFactory queryFactory, FaqCategoryJpaRepository faqCategoryJpaRepository) {
        this.queryFactory = queryFactory;
        this.faqCategoryJpaRepository = faqCategoryJpaRepository;
    }

    @Override
    public Optional<FaqCategory> findById(FaqCategoryId faqCategoryId) {
        if (faqCategoryId == null) {
            return Optional.empty();
        }
        FaqCategoryJpaEntity entity = queryFactory
            .selectFrom(faqCategoryJpaEntity)
            .where(faqCategoryJpaEntity.id.eq(faqCategoryId.value()), faqCategoryJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(FaqCategoryMapper::toDomain);
    }

    /**
     * 소속 FAQ 항목 존재 여부 — 삭제되지 않은(deleted=false) 항목이면 비노출(visible=false)이어도 존재로 본다.
     * 메서드명의 "Active"는 노출 여부가 아니라 미삭제를 뜻한다(전환 이전부터의 동작을 그대로 보존).
     */
    @Override
    public boolean existsActiveItemsByCategoryId(FaqCategoryId faqCategoryId) {
        Integer result = queryFactory
            .selectOne()
            .from(faqJpaEntity)
            .where(faqJpaEntity.faqCategoryId.eq(faqCategoryId), faqJpaEntity.deleted.isFalse())
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
