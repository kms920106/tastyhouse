package com.tastyhouse.infrastructure.persistence.sms.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;
import com.tastyhouse.application.sms.port.out.write.SmsVerificationLoadPort;
import com.tastyhouse.application.sms.port.out.write.SmsVerificationSavePort;

import static com.tastyhouse.infrastructure.persistence.sms.persistence.QSmsVerificationJpaEntity.smsVerificationJpaEntity;

@Repository
class SmsVerificationPersistenceAdapter implements SmsVerificationLoadPort, SmsVerificationSavePort {

    private final SmsVerificationJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    public SmsVerificationPersistenceAdapter(SmsVerificationJpaRepository jpaRepository, JPAQueryFactory queryFactory) {
        this.jpaRepository = jpaRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public SmsVerification save(SmsVerification smsVerification) {
        if (smsVerification.getId() == null) {
            SmsVerificationJpaEntity saved = jpaRepository.save(SmsVerificationMapper.toEntity(smsVerification));
            return SmsVerificationMapper.toDomain(saved);
        }

        SmsVerificationJpaEntity entity = jpaRepository.findById(smsVerification.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 SMS 인증입니다: " + smsVerification.getId()));
        SmsVerificationMapper.applyChanges(entity, smsVerification);
        return SmsVerificationMapper.toDomain(entity);
    }

    @Override
    public Optional<SmsVerification> findLatestPendingByPhoneNumber(String phoneNumber, SmsVerificationStatus status) {
        SmsVerificationJpaEntity entity = queryFactory
            .selectFrom(smsVerificationJpaEntity)
            .where(
                smsVerificationJpaEntity.phoneNumber.value.eq(phoneNumber),
                smsVerificationJpaEntity.status.eq(status == null ? null : status.name())
            )
            .orderBy(smsVerificationJpaEntity.createdAt.desc())
            .limit(1)
            .fetchOne();
        return Optional.ofNullable(entity).map(SmsVerificationMapper::toDomain);
    }

    @Override
    public void expireAllPendingByPhoneNumber(String phoneNumber) {
        queryFactory
            .update(smsVerificationJpaEntity)
            .set(smsVerificationJpaEntity.status, SmsVerificationStatus.EXPIRED.name())
            .where(
                smsVerificationJpaEntity.phoneNumber.value.eq(phoneNumber),
                smsVerificationJpaEntity.status.eq(SmsVerificationStatus.PENDING.name())
            )
            .execute();
    }
}
