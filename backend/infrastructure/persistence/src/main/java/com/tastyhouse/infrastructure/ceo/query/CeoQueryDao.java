package com.tastyhouse.infrastructure.ceo.query;

import com.tastyhouse.application.ceo.port.out.CeoQueryPort;
import com.tastyhouse.application.ceo.port.out.CeoListItemResult;
import com.querydsl.core.types.Projections;
import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

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
                ceoJpaEntity.status
            ))
            .from(ceoJpaEntity)
            .orderBy(ceoJpaEntity.name.asc())
            .fetch();
    }
}
