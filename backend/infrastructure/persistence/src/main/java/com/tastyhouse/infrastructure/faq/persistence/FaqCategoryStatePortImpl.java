package com.tastyhouse.infrastructure.faq.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.faq.port.out.write.FaqCategoryState;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryStatePort;

import static com.tastyhouse.infrastructure.faq.persistence.QFaqCategoryJpaEntity.faqCategoryJpaEntity;
import static com.tastyhouse.infrastructure.faq.persistence.QFaqJpaEntity.faqJpaEntity;

@Repository
public class FaqCategoryStatePortImpl implements FaqCategoryStatePort {
    private final JPAQueryFactory queryFactory;
    private final FaqCategoryJpaRepository faqCategoryJpaRepository;

    public FaqCategoryStatePortImpl(JPAQueryFactory queryFactory, FaqCategoryJpaRepository faqCategoryJpaRepository) {
        this.queryFactory = queryFactory;
        this.faqCategoryJpaRepository = faqCategoryJpaRepository;
    }

    @Override
    public Optional<FaqCategoryState> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        FaqCategoryJpaEntity entity = queryFactory
            .selectFrom(faqCategoryJpaEntity)
            .where(faqCategoryJpaEntity.id.eq(id), faqCategoryJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(FaqCategoryMapper::toState);
    }

    @Override
    public boolean existsActiveItemsByCategoryId(Long faqCategoryId) {
        Integer result = queryFactory
            .selectOne()
            .from(faqJpaEntity)
            .where(faqJpaEntity.faqCategoryId.eq(faqCategoryId), faqJpaEntity.deleted.isFalse())
            .fetchFirst();
        return result != null;
    }

    @Override
    public FaqCategoryState save(FaqCategoryState state) {
        if (state.id() == null) {
            FaqCategoryJpaEntity saved = faqCategoryJpaRepository.save(FaqCategoryMapper.toEntity(state));
            return FaqCategoryMapper.toState(saved);
        }

        FaqCategoryJpaEntity entity = faqCategoryJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 FAQ 카테고리입니다: " + state.id()));
        FaqCategoryMapper.applyChanges(entity, state);
        return FaqCategoryMapper.toState(entity);
    }
}
