package com.tastyhouse.core.repository.verification;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.verification.PhoneVerification;
import com.tastyhouse.core.entity.verification.PhoneVerificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.tastyhouse.core.entity.verification.QPhoneVerification.phoneVerification;

@Repository
@RequiredArgsConstructor
public class PhoneVerificationRepositoryImpl implements PhoneVerificationRepository {

    private final JPAQueryFactory queryFactory;

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

    @Override
    public void expireAllOverdue(LocalDateTime now) {
        queryFactory
            .update(phoneVerification)
            .set(phoneVerification.status, PhoneVerificationStatus.EXPIRED)
            .where(
                phoneVerification.status.eq(PhoneVerificationStatus.PENDING),
                phoneVerification.expiresAt.lt(now)
            )
            .execute();
    }
}
