package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.domain.member.model.MemberWithdrawal;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 회원 탈퇴 이력 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 * update 경로가 없어(insert 전용) {@code applyChanges}는 두지 않는다.
 */
final class MemberWithdrawalMapper {

    private MemberWithdrawalMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static MemberWithdrawal toDomain(MemberWithdrawalJpaEntity entity) {
        return MemberWithdrawal.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getReason(),
            entity.getReasonDetail(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static MemberWithdrawalJpaEntity toEntity(MemberWithdrawal domain) {
        return MemberWithdrawalJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getReason(),
            domain.getReasonDetail()
        );
    }
}
