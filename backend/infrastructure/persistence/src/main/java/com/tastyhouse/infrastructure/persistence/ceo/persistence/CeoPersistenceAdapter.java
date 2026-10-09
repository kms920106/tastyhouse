package com.tastyhouse.infrastructure.persistence.ceo.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.application.ceo.port.out.write.CeoPersistencePort;

import static com.tastyhouse.infrastructure.persistence.ceo.persistence.QCeoJpaEntity.ceoJpaEntity;

@Repository
class CeoPersistenceAdapter implements CeoPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final CeoJpaRepository ceoJpaRepository;

    public CeoPersistenceAdapter(JPAQueryFactory queryFactory, CeoJpaRepository ceoJpaRepository) {
        this.queryFactory = queryFactory;
        this.ceoJpaRepository = ceoJpaRepository;
    }

    @Override
    public Optional<Ceo> findById(CeoId id) {
        return ceoJpaRepository.findById(id.value()).map(CeoMapper::toDomain);
    }

    @Override
    public Optional<Ceo> findByUsername(String username) {
        CeoJpaEntity entity = queryFactory
            .selectFrom(ceoJpaEntity)
            .where(ceoJpaEntity.username.eq(username))
            .fetchOne();
        return Optional.ofNullable(entity).map(CeoMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return queryFactory
            .selectOne()
            .from(ceoJpaEntity)
            .where(ceoJpaEntity.username.eq(username))
            .fetchFirst() != null;
    }

    @Override
    public Ceo save(Ceo ceo) {
        CeoJpaEntity saved = ceoJpaRepository.save(CeoMapper.toEntity(ceo));
        return CeoMapper.toDomain(saved);
    }
}
