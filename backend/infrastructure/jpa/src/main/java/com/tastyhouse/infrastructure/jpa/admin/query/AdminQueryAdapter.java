package com.tastyhouse.infrastructure.jpa.admin.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.admin.port.out.AdminQueryPort;

import static com.tastyhouse.infrastructure.jpa.admin.persistence.QAdminJpaEntity.adminJpaEntity;

@Repository
class AdminQueryAdapter implements AdminQueryPort {

    private final JPAQueryFactory queryFactory;

    public AdminQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public boolean existsByUsername(String username) {
        return queryFactory
            .selectOne()
            .from(adminJpaEntity)
            .where(adminJpaEntity.username.eq(username))
            .fetchFirst() != null;
    }
}
