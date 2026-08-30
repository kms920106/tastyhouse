package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 회원 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class MemberMapper {

    private MemberMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Member toDomain(MemberJpaEntity entity) {
        return Member.reconstitute(
            entity.getId(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getNickname(),
            entity.getFullName(),
            entity.getBirthDate(),
            entity.getGender(),
            entity.getPhoneNumber(),
            entity.getMemberGrade(),
            IdMapping.vo(entity.getProfileImageFileId(), UploadedFileId::of),
            entity.getStatusMessage(),
            entity.isPushNotificationEnabled(),
            entity.isMarketingInfoEnabled(),
            entity.isEventInfoEnabled(),
            entity.getMemberStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static MemberJpaEntity toEntity(Member domain) {
        return MemberJpaEntity.create(
            domain.getUsername(),
            domain.getPassword(),
            domain.getNickname(),
            domain.getFullName(),
            domain.getBirthDate(),
            domain.getGender(),
            domain.getPhoneNumber(),
            domain.getMemberGrade(),
            IdMapping.raw(domain.getProfileImageFileId(), UploadedFileId::value),
            domain.getStatusMessage(),
            domain.isPushNotificationEnabled(),
            domain.isMarketingInfoEnabled(),
            domain.isEventInfoEnabled(),
            domain.getMemberStatus()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(MemberJpaEntity entity, Member domain) {
        entity.applyChanges(
            domain.getPassword(),
            domain.getNickname(),
            domain.getFullName(),
            domain.getBirthDate(),
            domain.getGender(),
            domain.getPhoneNumber(),
            IdMapping.raw(domain.getProfileImageFileId(), UploadedFileId::value),
            domain.getStatusMessage(),
            domain.isPushNotificationEnabled(),
            domain.isMarketingInfoEnabled(),
            domain.isEventInfoEnabled(),
            domain.getMemberStatus()
        );
    }
}
