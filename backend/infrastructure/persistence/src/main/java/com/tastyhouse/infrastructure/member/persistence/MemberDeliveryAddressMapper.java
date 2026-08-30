package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 회원 배달 주소록 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을
 * infrastructure에 둔다.
 *
 * <p>{@code admin_dong_id}는 행정동 매칭 실패 시 null이므로 {@link IdMapping}을 거쳐야 한다 —
 * {@code AdminDongId.of(null)}은 VO의 compact constructor에서 예외가 난다.
 */
final class MemberDeliveryAddressMapper {

    private MemberDeliveryAddressMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static MemberDeliveryAddress toDomain(MemberDeliveryAddressJpaEntity entity) {
        return MemberDeliveryAddress.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getAlias(),
            entity.getRoadAddress(),
            entity.getLotAddress(),
            entity.getDetailAddress(),
            IdMapping.vo(entity.getAdminDongId(), AdminDongId::of),
            entity.getLatitude(),
            entity.getLongitude(),
            entity.isDefaultAddress(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static MemberDeliveryAddressJpaEntity toEntity(MemberDeliveryAddress domain) {
        return MemberDeliveryAddressJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getAlias(),
            domain.getRoadAddress(),
            domain.getLotAddress(),
            domain.getDetailAddress(),
            IdMapping.raw(domain.getAdminDongId(), AdminDongId::value),
            domain.getLatitude(),
            domain.getLongitude(),
            domain.isDefaultAddress()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(MemberDeliveryAddressJpaEntity entity, MemberDeliveryAddress domain) {
        entity.applyChanges(
            domain.getAlias(),
            domain.getRoadAddress(),
            domain.getLotAddress(),
            domain.getDetailAddress(),
            IdMapping.raw(domain.getAdminDongId(), AdminDongId::value),
            domain.getLatitude(),
            domain.getLongitude(),
            domain.isDefaultAddress()
        );
    }
}
