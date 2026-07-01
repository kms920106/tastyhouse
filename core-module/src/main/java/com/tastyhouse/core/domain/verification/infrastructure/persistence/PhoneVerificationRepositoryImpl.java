package com.tastyhouse.core.domain.verification.infrastructure.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.verification.domain.model.PhoneVerification;
import com.tastyhouse.core.domain.verification.domain.model.PhoneVerificationStatus;
import com.tastyhouse.core.domain.verification.domain.repository.PhoneVerificationRepository;

import static com.tastyhouse.core.domain.verification.domain.model.QPhoneVerification.phoneVerification;

@Repository
@RequiredArgsConstructor
public class PhoneVerificationRepositoryImpl implements PhoneVerificationRepository {

    private final PhoneVerificationJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public PhoneVerification save(PhoneVerification phoneVerification) {
        return jpaRepository.save(phoneVerification);
    }

    @Override
    public Optional<PhoneVerification> findLatestPendingByPhoneNumber(String phoneNumber, PhoneVerificationStatus status) {
        PhoneVerification result = queryFactory
            .selectFrom(phoneVerification)
            .where(
                phoneVerification.phoneNumber.value.eq(phoneNumber),
                phoneVerification.status.eq(status)
            )
            .orderBy(phoneVerification.createdAt.desc())
            .limit(1)
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public void expireAllPendingByPhoneNumber(String phoneNumber) {
        queryFactory
            .update(phoneVerification)
            .set(phoneVerification.status, PhoneVerificationStatus.EXPIRED)
            .where(
                phoneVerification.phoneNumber.value.eq(phoneNumber),
                phoneVerification.status.eq(PhoneVerificationStatus.PENDING)
            )
            .execute();
    }
}
