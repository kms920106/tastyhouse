package com.tastyhouse.infrastructure.faq.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.faq.port.out.write.FaqState;
import com.tastyhouse.application.faq.port.out.write.FaqStatePort;

import static com.tastyhouse.infrastructure.faq.persistence.QFaqJpaEntity.faqJpaEntity;

@Repository
public class FaqStatePortImpl implements FaqStatePort {
    private final JPAQueryFactory queryFactory;
    private final FaqJpaRepository faqJpaRepository;

    public FaqStatePortImpl(JPAQueryFactory queryFactory, FaqJpaRepository faqJpaRepository) {
        this.queryFactory = queryFactory;
        this.faqJpaRepository = faqJpaRepository;
    }

    @Override
    public Optional<FaqState> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        FaqJpaEntity entity = queryFactory
            .selectFrom(faqJpaEntity)
            .where(faqJpaEntity.id.eq(id), faqJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(FaqMapper::toState);
    }

    @Override
    public FaqState save(FaqState state) {
        if (state.id() == null) {
            FaqJpaEntity saved = faqJpaRepository.save(FaqMapper.toEntity(state));
            return FaqMapper.toState(saved);
        }

        FaqJpaEntity entity = faqJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 FAQ입니다: " + state.id()));
        FaqMapper.applyChanges(entity, state);
        return FaqMapper.toState(entity);
    }
}
