package com.tastyhouse.infrastructure.persistence.mail.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;
import com.tastyhouse.application.mail.port.out.write.MailVerificationPersistencePort;

import static com.tastyhouse.infrastructure.persistence.mail.persistence.QMailVerificationJpaEntity.mailVerificationJpaEntity;

@Repository
class MailVerificationPersistenceAdapter implements MailVerificationPersistencePort {

    private final MailVerificationJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    public MailVerificationPersistenceAdapter(MailVerificationJpaRepository jpaRepository, JPAQueryFactory queryFactory) {
        this.jpaRepository = jpaRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public MailVerification save(MailVerification mailVerification) {
        if (mailVerification.getId() == null) {
            MailVerificationJpaEntity saved = jpaRepository.save(MailVerificationMapper.toEntity(mailVerification));
            return MailVerificationMapper.toDomain(saved);
        }

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
                mailVerificationJpaEntity.status.eq(status == null ? null : status.name())
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
            .set(mailVerificationJpaEntity.status, MailVerificationStatus.EXPIRED.name())
            .where(
                mailVerificationJpaEntity.email.eq(email),
                mailVerificationJpaEntity.status.eq(MailVerificationStatus.PENDING.name())
            )
            .execute();
    }
}
