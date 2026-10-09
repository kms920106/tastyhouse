package com.tastyhouse.infrastructure.jpa.faq.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqId;
import com.tastyhouse.application.faq.port.out.write.FaqLoadPort;
import com.tastyhouse.application.faq.port.out.write.FaqSavePort;

import static com.tastyhouse.infrastructure.jpa.faq.persistence.QFaqJpaEntity.faqJpaEntity;

@Repository
class FaqPersistenceAdapter implements FaqLoadPort, FaqSavePort {

    private final JPAQueryFactory queryFactory;
    private final FaqJpaRepository faqJpaRepository;

    public FaqPersistenceAdapter(JPAQueryFactory queryFactory, FaqJpaRepository faqJpaRepository) {
        this.queryFactory = queryFactory;
        this.faqJpaRepository = faqJpaRepository;
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

        FaqJpaEntity entity = faqJpaRepository.findById(faq.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 FAQ입니다: " + faq.getId()));
        FaqMapper.applyChanges(entity, faq);
        return FaqMapper.toDomain(entity);
    }
}
