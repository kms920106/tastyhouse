package com.tastyhouse.infrastructure.sms.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.sms.port.out.SmsVerificationStatusCodes;
import com.tastyhouse.application.sms.port.out.write.SmsVerificationState;
import com.tastyhouse.application.sms.port.out.write.SmsVerificationStatePort;

import static com.tastyhouse.infrastructure.sms.persistence.QSmsVerificationJpaEntity.smsVerificationJpaEntity;

@Repository
public class SmsVerificationStatePortImpl implements SmsVerificationStatePort {
    private final SmsVerificationJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    public SmsVerificationStatePortImpl(SmsVerificationJpaRepository jpaRepository, JPAQueryFactory queryFactory) {
        this.jpaRepository = jpaRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public SmsVerificationState save(SmsVerificationState state) {
        if (state.id() == null) {
            SmsVerificationJpaEntity saved = jpaRepository.save(SmsVerificationMapper.toEntity(state));
            return SmsVerificationMapper.toState(saved);
        }

        SmsVerificationJpaEntity entity = jpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 SMS 인증입니다: " + state.id()));
        SmsVerificationMapper.applyChanges(entity, state);
        return SmsVerificationMapper.toState(entity);
    }

    @Override
    public Optional<SmsVerificationState> findLatestPendingByPhoneNumber(String phoneNumber, String status) {
        SmsVerificationJpaEntity result = queryFactory
            .selectFrom(smsVerificationJpaEntity)
            .where(
                smsVerificationJpaEntity.phoneNumber.value.eq(phoneNumber),
                smsVerificationJpaEntity.status.eq(status)
            )
            .orderBy(smsVerificationJpaEntity.createdAt.desc())
            .limit(1)
            .fetchOne();
        return Optional.ofNullable(result).map(SmsVerificationMapper::toState);
    }

    @Override
    public void expireAllPendingByPhoneNumber(String phoneNumber) {
        queryFactory
            .update(smsVerificationJpaEntity)
            .set(smsVerificationJpaEntity.status, SmsVerificationStatusCodes.EXPIRED)
            .where(
                smsVerificationJpaEntity.phoneNumber.value.eq(phoneNumber),
                smsVerificationJpaEntity.status.eq(SmsVerificationStatusCodes.PENDING)
            )
            .execute();
    }
}
