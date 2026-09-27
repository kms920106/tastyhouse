package com.tastyhouse.infrastructure.ceo.query;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.ceo.port.out.CeoListItemResult;
import com.tastyhouse.application.ceo.port.out.CeoQueryPort;

import static com.tastyhouse.infrastructure.ceo.persistence.QCeoJpaEntity.ceoJpaEntity;

@Repository
public class CeoQueryDao implements CeoQueryPort {
    private final JPAQueryFactory queryFactory;

    public CeoQueryDao(JPAQueryFactory queryFactory) {
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
}
