package com.tastyhouse.domain.member.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class MemberTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 신입멤버·활성 상태다")
    void of_createsTransientMember() {
        Member member = Member.of(
            "user1", "encodedPw", "닉네임", "홍길동", MemberGender.MALE, 19900101,
            "01012345678", true, true, true
        );

        assertThat(member.getId()).isNull();
        assertThat(member.getUsername()).isEqualTo("user1");
        assertThat(member.getPassword()).isEqualTo("encodedPw");
        assertThat(member.getNickname()).isEqualTo("닉네임");
        assertThat(member.getFullName()).isEqualTo("홍길동");
        assertThat(member.getGender()).isEqualTo(MemberGender.MALE);
        assertThat(member.getBirthDate()).isEqualTo(19900101);
        assertThat(member.getPhoneNumber().value()).isEqualTo("01012345678");
        assertThat(member.getMemberGrade()).isEqualTo(MemberGrade.NEWCOMER);
        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getCreatedAt()).isNull();
        assertThat(member.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("ofSocial로 생성하면 비밀번호 없이 소셜 회원이 생성된다")
    void ofSocial_createsMemberWithoutPassword() {
        Member member = Member.ofSocial(
            "social1", "닉네임", "홍길동", MemberGender.FEMALE, 19950505,
            "01099998888", false, false, false
        );

        assertThat(member.getPassword()).isNull();
        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getMemberGrade()).isEqualTo(MemberGrade.NEWCOMER);
    }

    @Test
    @DisplayName("updateProfile은 null이 아닌 필드만 변경한다")
    void updateProfile_changesOnlyNonNullFields() {
        Member member = Member.of(
            "user1", "pw", "닉네임", "홍길동", MemberGender.MALE, 19900101,
            "01012345678", true, true, true
        );

        member.updateProfile("새닉네임", null, UploadedFileId.of(100L));

        assertThat(member.getNickname()).isEqualTo("새닉네임");
        assertThat(member.getStatusMessage()).isNull();
        assertThat(member.getProfileImageFileId()).isEqualTo(UploadedFileId.of(100L));
    }

    @Test
    @DisplayName("updatePersonalInfo는 전화번호를 포함한 개인정보를 변경한다")
    void updatePersonalInfo_changesFields() {
        Member member = Member.of(
            "user1", "pw", "닉네임", "홍길동", MemberGender.MALE, 19900101,
            "01012345678", true, true, true
        );

        member.updatePersonalInfo("새이름", "01099998888", 20000101, MemberGender.FEMALE, false, false, false);

        assertThat(member.getFullName()).isEqualTo("새이름");
        assertThat(member.getPhoneNumber().value()).isEqualTo("01099998888");
        assertThat(member.getBirthDate()).isEqualTo(20000101);
        assertThat(member.getGender()).isEqualTo(MemberGender.FEMALE);
        assertThat(member.isPushNotificationEnabled()).isFalse();
        assertThat(member.isMarketingInfoEnabled()).isFalse();
        assertThat(member.isEventInfoEnabled()).isFalse();
    }

    @Test
    @DisplayName("withdraw는 상태를 DELETED로 바꾸고, 이미 탈퇴한 회원은 재탈퇴할 수 없다")
    void withdraw_marksDeleted_andRejectsDouble() {
        Member member = Member.of(
            "user1", "pw", "닉네임", "홍길동", MemberGender.MALE, 19900101,
            "01012345678", true, true, true
        );

        member.withdraw();

        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.DELETED);
        assertThatThrownBy(member::withdraw).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("suspend는 활성 회원을 정지시키고, 이미 정지된 회원은 재정지할 수 없다")
    void suspend_marksSuspended_andRejectsDouble() {
        Member member = Member.of(
            "user1", "pw", "닉네임", "홍길동", MemberGender.MALE, 19900101,
            "01012345678", true, true, true
        );

        member.suspend();

        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.SUSPENDED);
        assertThatThrownBy(member::suspend).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("탈퇴한 회원은 정지할 수 없다")
    void suspend_onWithdrawn_throws() {
        Member member = Member.of(
            "user1", "pw", "닉네임", "홍길동", MemberGender.MALE, 19900101,
            "01012345678", true, true, true
        );
        member.withdraw();

        assertThatThrownBy(member::suspend).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("activate는 정지된 회원을 활성화하고, 이미 활성인 회원은 재활성화할 수 없다")
    void activate_reactivatesSuspended_andRejectsDouble() {
        Member member = Member.of(
            "user1", "pw", "닉네임", "홍길동", MemberGender.MALE, 19900101,
            "01012345678", true, true, true
        );
        member.suspend();

        member.activate();

        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThatThrownBy(member::activate).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("탈퇴한 회원은 활성화할 수 없다")
    void activate_onWithdrawn_throws() {
        Member member = Member.of(
            "user1", "pw", "닉네임", "홍길동", MemberGender.MALE, 19900101,
            "01012345678", true, true, true
        );
        member.withdraw();

        assertThatThrownBy(member::activate).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        Member member = Member.reconstitute(
            1L, "user1", "pw", "닉네임", "홍길동", 19900101, MemberGender.MALE,
            new PhoneNumber("01012345678"), MemberGrade.ACTIVE, UploadedFileId.of(100L), "상태메시지",
            true, true, true, MemberStatus.ACTIVE, createdAt, updatedAt
        );

        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getMemberId()).isEqualTo(MemberId.of(1L));
        assertThat(member.getMemberGrade()).isEqualTo(MemberGrade.ACTIVE);
        assertThat(member.getCreatedAt()).isEqualTo(createdAt);
        assertThat(member.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
