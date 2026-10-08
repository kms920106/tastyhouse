package com.tastyhouse.infrastructure.persistence.ceo.query;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.ceo.port.out.CeoListItemResult;
import com.tastyhouse.application.ceo.port.out.CeoOwnerQueryPort;
import com.tastyhouse.application.ceo.port.out.CeoQueryPort;

import static com.tastyhouse.infrastructure.persistence.ceo.persistence.QCeoJpaEntity.ceoJpaEntity;

@Repository
class CeoQueryAdapter implements CeoQueryPort, CeoOwnerQueryPort {

    private final JPAQueryFactory queryFactory;

    public CeoQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<CeoListItemResult> findAllCeos() {
        return queryFactory
            .select(Projections.constructor(CeoListItemResult.class,
                ceoJpaEntity.id,
                ceoJpaEntity.name,
                ceoJpaEntity.businessRegistrationNumber,
                ceoJpaEntity.status.stringValue()
            ))
            .from(ceoJpaEntity)
            .orderBy(ceoJpaEntity.name.asc())
            .fetch();
    }

    @Override
    public boolean existsByUsername(String username) {
        return queryFactory
            .selectOne()
            .from(ceoJpaEntity)
            .where(ceoJpaEntity.username.eq(username))
            .fetchFirst() != null;
    }
}
