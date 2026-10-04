package com.tastyhouse.infrastructure.persistence.member.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

import static org.assertj.core.api.Assertions.assertThat;

class MemberMapperTest {

    @Test
    @DisplayName("Member → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void memberToEntity() {
        Member original = fullMember();

        MemberJpaEntity entity = MemberMapper.toEntity(original);

        assertThat(entity.getUsername()).isEqualTo("user@test.com");
        assertThat(entity.getPassword()).isEqualTo("encoded-pw");
        assertThat(entity.getNickname()).isEqualTo("닉네임");
        assertThat(entity.getFullName()).isEqualTo("홍길동");
        assertThat(entity.getBirthDate()).isEqualTo(19900102);
        assertThat(entity.getGender()).isEqualTo("FEMALE");
        assertThat(entity.getPhoneNumber().value()).isEqualTo("01012345678");
        assertThat(entity.getMemberGrade()).isEqualTo("INSIDER");
        assertThat(entity.getProfileImageFileId()).isEqualTo(82L);
        assertThat(entity.getStatusMessage()).isEqualTo("상태 메시지");
        assertThat(entity.isPushNotificationEnabled()).isTrue();
        assertThat(entity.isMarketingInfoEnabled()).isFalse();
        assertThat(entity.isEventInfoEnabled()).isTrue();
        assertThat(entity.getMemberStatus()).isEqualTo("SUSPENDED");
    }

    @Test
    @DisplayName("엔티티 → Member 변환 시 모든 필드가 보존된다")
    void memberToDomain() {
        Member original = fullMember();

        Member restored = MemberMapper.toDomain(entityOf(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("Member의 boolean 필드가 뒤바뀌지 않는다")
    void memberBooleanFieldsAreNotSwapped() {
        Member original = Member.reconstitute(
            83L, "u2", null, "n2", "f2", 20000101,
            MemberGender.MALE, new PhoneNumber("01098765432"), MemberGrade.NEWCOMER,
            null, null,
            false, true, false,
            MemberStatus.ACTIVE,
            LocalDateTime.of(2026, 3, 3, 3, 3),
            LocalDateTime.of(2026, 4, 4, 4, 4));

        MemberJpaEntity entity = MemberMapper.toEntity(original);

        assertThat(entity.isPushNotificationEnabled()).isFalse();
        assertThat(entity.isMarketingInfoEnabled()).isTrue();
        assertThat(entity.isEventInfoEnabled()).isFalse();
        assertThat(entity.getProfileImageFileId()).isNull();
        assertThat(MemberMapper.toDomain(entityOf(original))).usingRecursiveComparison().isEqualTo(original);
    }

    private static Member fullMember() {
        return Member.reconstitute(
            81L, "user@test.com", "encoded-pw", "닉네임", "홍길동", 19900102,
            MemberGender.FEMALE, new PhoneNumber("01012345678"), MemberGrade.INSIDER,
            UploadedFileId.of(82L), "상태 메시지",
            true, false, true,
            MemberStatus.SUSPENDED,
            LocalDateTime.of(2026, 1, 1, 1, 1),
            LocalDateTime.of(2026, 2, 2, 2, 2));
    }

    private static MemberJpaEntity entityOf(Member member) {
        MemberJpaEntity entity = MemberMapper.toEntity(member);
        ReflectionTestUtils.setField(entity, "id", member.getId());
        ReflectionTestUtils.setField(entity, "createdAt", member.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", member.getUpdatedAt());
        return entity;
    }
}
