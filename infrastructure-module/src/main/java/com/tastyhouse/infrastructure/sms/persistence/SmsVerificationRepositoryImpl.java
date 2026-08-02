package com.tastyhouse.infrastructure.sms.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.sms.domain.model.SmsVerification;
import com.tastyhouse.domain.sms.domain.model.SmsVerificationStatus;
import com.tastyhouse.domain.sms.domain.repository.SmsVerificationRepository;

import static com.tastyhouse.infrastructure.sms.persistence.QSmsVerificationJpaEntity.smsVerificationJpaEntity;

@Repository
public class SmsVerificationRepositoryImpl implements SmsVerificationRepository {

    private final SmsVerificationJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    public SmsVerificationRepositoryImpl(SmsVerificationJpaRepository jpaRepository, JPAQueryFactory queryFactory) {
        this.jpaRepository = jpaRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public SmsVerification save(SmsVerification smsVerification) {
        if (smsVerification.getId() == null) {
            SmsVerificationJpaEntity saved = jpaRepository.save(SmsVerificationMapper.toEntity(smsVerification));
            return SmsVerificationMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 감사 필드 파손 위험이 있어 쓰지 않는다.
        SmsVerificationJpaEntity entity = jpaRepository.findById(smsVerification.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 SMS 인증입니다: " + smsVerification.getId()));
        SmsVerificationMapper.applyChanges(entity, smsVerification);
        return SmsVerificationMapper.toDomain(entity);
    }

    @Override
    public Optional<SmsVerification> findLatestPendingByPhoneNumber(String phoneNumber, SmsVerificationStatus status) {
        SmsVerificationJpaEntity result = queryFactory
            .selectFrom(smsVerificationJpaEntity)
            .where(
                smsVerificationJpaEntity.phoneNumber.value.eq(phoneNumber),
                smsVerificationJpaEntity.status.eq(status)
            )
            .orderBy(smsVerificationJpaEntity.createdAt.desc())
            .limit(1)
            .fetchOne();
        return Optional.ofNullable(result).map(SmsVerificationMapper::toDomain);
    }

    @Override
    public void expireAllPendingByPhoneNumber(String phoneNumber) {
        queryFactory
            .update(smsVerificationJpaEntity)
            .set(smsVerificationJpaEntity.status, SmsVerificationStatus.EXPIRED)
            .where(
                smsVerificationJpaEntity.phoneNumber.value.eq(phoneNumber),
                smsVerificationJpaEntity.status.eq(SmsVerificationStatus.PENDING)
            )
            .execute();
    }
}
