package com.tastyhouse.infrastructure.mail.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;
import com.tastyhouse.domain.mail.repository.MailVerificationRepository;

import static com.tastyhouse.infrastructure.mail.persistence.QMailVerificationJpaEntity.mailVerificationJpaEntity;

@Repository
public class MailVerificationRepositoryImpl implements MailVerificationRepository {

    private final MailVerificationJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    public MailVerificationRepositoryImpl(MailVerificationJpaRepository jpaRepository, JPAQueryFactory queryFactory) {
        this.jpaRepository = jpaRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public MailVerification save(MailVerification mailVerification) {
        if (mailVerification.getId() == null) {
            MailVerificationJpaEntity saved = jpaRepository.save(MailVerificationMapper.toEntity(mailVerification));
            return MailVerificationMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 감사 필드 파손 위험이 있어 쓰지 않는다.
        MailVerificationJpaEntity entity = jpaRepository.findById(mailVerification.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메일 인증입니다: " + mailVerification.getId()));
        MailVerificationMapper.applyChanges(entity, mailVerification);
        return MailVerificationMapper.toDomain(entity);
    }

    @Override
    public Optional<MailVerification> findLatestPendingByEmail(String email, MailVerificationStatus status) {
        MailVerificationJpaEntity result = queryFactory
            .selectFrom(mailVerificationJpaEntity)
            .where(
                mailVerificationJpaEntity.email.eq(email),
                mailVerificationJpaEntity.status.eq(status)
            )
            .orderBy(mailVerificationJpaEntity.createdAt.desc())
            .limit(1)
            .fetchOne();
        return Optional.ofNullable(result).map(MailVerificationMapper::toDomain);
    }

    @Override
    public void expireAllPendingByEmail(String email) {
        queryFactory
            .update(mailVerificationJpaEntity)
            .set(mailVerificationJpaEntity.status, MailVerificationStatus.EXPIRED)
            .where(
                mailVerificationJpaEntity.email.eq(email),
                mailVerificationJpaEntity.status.eq(MailVerificationStatus.PENDING)
            )
            .execute();
    }
}
