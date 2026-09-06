package com.tastyhouse.infrastructure.faq.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.repository.FaqCategoryRepository;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;

import static com.tastyhouse.infrastructure.faq.persistence.QFaqCategoryJpaEntity.faqCategoryJpaEntity;
import static com.tastyhouse.infrastructure.faq.persistence.QFaqJpaEntity.faqJpaEntity;

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

        FaqCategoryJpaEntity entity = faqCategoryJpaRepository.findById(faqCategory.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 FAQ 카테고리입니다: " + faqCategory.getId()));
        FaqCategoryMapper.applyChanges(entity, faqCategory);
        return FaqCategoryMapper.toDomain(entity);
    }
}
