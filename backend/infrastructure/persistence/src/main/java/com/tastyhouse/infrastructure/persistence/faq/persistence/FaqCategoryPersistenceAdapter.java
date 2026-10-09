package com.tastyhouse.infrastructure.persistence.faq.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryLoadPort;
import com.tastyhouse.application.faq.port.out.write.FaqCategorySavePort;

import static com.tastyhouse.infrastructure.persistence.faq.persistence.QFaqCategoryJpaEntity.faqCategoryJpaEntity;
import static com.tastyhouse.infrastructure.persistence.faq.persistence.QFaqJpaEntity.faqJpaEntity;

@Repository
class FaqCategoryPersistenceAdapter implements FaqCategoryLoadPort, FaqCategorySavePort {

    private final JPAQueryFactory queryFactory;
    private final FaqCategoryJpaRepository faqCategoryJpaRepository;

    public FaqCategoryPersistenceAdapter(JPAQueryFactory queryFactory, FaqCategoryJpaRepository faqCategoryJpaRepository) {
        this.queryFactory = queryFactory;
        this.faqCategoryJpaRepository = faqCategoryJpaRepository;
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
        return queryFactory
            .selectOne()
            .from(faqJpaEntity)
            .where(faqJpaEntity.faqCategoryId.eq(faqCategoryId.value()), faqJpaEntity.deleted.isFalse())
            .fetchFirst() != null;
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
