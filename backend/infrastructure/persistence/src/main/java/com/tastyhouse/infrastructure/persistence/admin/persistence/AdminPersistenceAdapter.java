package com.tastyhouse.infrastructure.persistence.admin.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.application.admin.port.out.write.AdminPersistencePort;

import static com.tastyhouse.infrastructure.persistence.admin.persistence.QAdminJpaEntity.adminJpaEntity;

@Repository
class AdminPersistenceAdapter implements AdminPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final AdminJpaRepository adminJpaRepository;

    public AdminPersistenceAdapter(JPAQueryFactory queryFactory, AdminJpaRepository adminJpaRepository) {
        this.queryFactory = queryFactory;
        this.adminJpaRepository = adminJpaRepository;
    }

    @Override
    public Optional<Admin> findByUsername(String username) {
        return Optional.ofNullable(queryFactory
                .selectFrom(adminJpaEntity)
                .where(adminJpaEntity.username.eq(username))
                .fetchOne())
            .map(AdminMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return queryFactory
            .selectOne()
            .from(adminJpaEntity)
            .where(adminJpaEntity.username.eq(username))
            .fetchFirst() != null;
    }

    @Override
    public Admin save(Admin admin) {
        AdminJpaEntity saved = adminJpaRepository.save(AdminMapper.toEntity(admin));
        return AdminMapper.toDomain(saved);
    }
}
