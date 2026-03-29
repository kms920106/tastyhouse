package com.tastyhouse.core.repository.verification;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.verification.PhoneVerification;
import com.tastyhouse.core.entity.verification.PhoneVerificationStatus;
import com.tastyhouse.core.entity.verification.QPhoneVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PhoneVerificationRepositoryImpl implements PhoneVerificationRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<PhoneVerification> findLatestPendingByPhoneNumber(String phoneNumber, PhoneVerificationStatus status) {
        QPhoneVerification pv = QPhoneVerification.phoneVerification;
        PhoneVerification result = queryFactory
            .selectFrom(pv)
            .where(
                pv.phoneNumber.value.eq(phoneNumber),
                pv.status.eq(status)
            )
            .orderBy(pv.createdAt.desc())
            .limit(1)
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public void expireAllPendingByPhoneNumber(String phoneNumber) {
        QPhoneVerification pv = QPhoneVerification.phoneVerification;
        queryFactory
            .update(pv)
            .set(pv.status, PhoneVerificationStatus.EXPIRED)
            .where(
                pv.phoneNumber.value.eq(phoneNumber),
                pv.status.eq(PhoneVerificationStatus.PENDING)
            )
            .execute();
    }

    @Override
    public void expireAllOverdue(LocalDateTime now) {
        QPhoneVerification pv = QPhoneVerification.phoneVerification;
        queryFactory
            .update(pv)
            .set(pv.status, PhoneVerificationStatus.EXPIRED)
            .where(
                pv.status.eq(PhoneVerificationStatus.PENDING),
                pv.expiresAt.lt(now)
            )
            .execute();
    }
}
