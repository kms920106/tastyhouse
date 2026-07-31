package com.tastyhouse.infrastructure.verification.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.verification.domain.model.PhoneVerification;
import com.tastyhouse.domain.verification.domain.model.PhoneVerificationStatus;
import com.tastyhouse.domain.verification.domain.repository.PhoneVerificationRepository;

import static com.tastyhouse.infrastructure.verification.persistence.QPhoneVerificationJpaEntity.phoneVerificationJpaEntity;

@Repository
@RequiredArgsConstructor
public class PhoneVerificationRepositoryImpl implements PhoneVerificationRepository {

    private final PhoneVerificationJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public PhoneVerification save(PhoneVerification phoneVerification) {
        if (phoneVerification.getId() == null) {
            PhoneVerificationJpaEntity saved = jpaRepository.save(PhoneVerificationMapper.toEntity(phoneVerification));
            return PhoneVerificationMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 감사 필드 파손 위험이 있어 쓰지 않는다.
        PhoneVerificationJpaEntity entity = jpaRepository.findById(phoneVerification.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 휴대폰 인증입니다: " + phoneVerification.getId()));
        PhoneVerificationMapper.applyChanges(entity, phoneVerification);
        return PhoneVerificationMapper.toDomain(entity);
    }

    @Override
    public Optional<PhoneVerification> findLatestPendingByPhoneNumber(String phoneNumber, PhoneVerificationStatus status) {
        PhoneVerificationJpaEntity result = queryFactory
            .selectFrom(phoneVerificationJpaEntity)
            .where(
                phoneVerificationJpaEntity.phoneNumber.value.eq(phoneNumber),
                phoneVerificationJpaEntity.status.eq(status)
            )
            .orderBy(phoneVerificationJpaEntity.createdAt.desc())
            .limit(1)
            .fetchOne();
        return Optional.ofNullable(result).map(PhoneVerificationMapper::toDomain);
    }

    @Override
    public void expireAllPendingByPhoneNumber(String phoneNumber) {
        queryFactory
            .update(phoneVerificationJpaEntity)
            .set(phoneVerificationJpaEntity.status, PhoneVerificationStatus.EXPIRED)
            .where(
                phoneVerificationJpaEntity.phoneNumber.value.eq(phoneNumber),
                phoneVerificationJpaEntity.status.eq(PhoneVerificationStatus.PENDING)
            )
            .execute();
    }
}
