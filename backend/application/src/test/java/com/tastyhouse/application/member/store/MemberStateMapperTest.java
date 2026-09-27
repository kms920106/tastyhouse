package com.tastyhouse.application.member.store;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.model.MemberWithdrawal;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

import static org.assertj.core.api.Assertions.assertThat;

class MemberStateMapperTest {

    @Test
    @DisplayName("Member → MemberState → Member 왕복 시 모든 필드가 보존된다")
    void memberRoundTrip() {
        Member original = Member.reconstitute(
            81L, "user@test.com", "encoded-pw", "닉네임", "홍길동", 19900102,
            MemberGender.FEMALE, new PhoneNumber("01012345678"), MemberGrade.INSIDER,
            UploadedFileId.of(82L), "상태 메시지",
            true, false, true,
            MemberStatus.SUSPENDED,
            LocalDateTime.of(2026, 1, 1, 1, 1),
            LocalDateTime.of(2026, 2, 2, 2, 2));

        Member restored = MemberStateMapper.toDomain(MemberStateMapper.toState(original));

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

        Member restored = MemberStateMapper.toDomain(MemberStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("MemberDeliveryAddress → MemberDeliveryAddressState → MemberDeliveryAddress 왕복 시 모든 필드가 보존된다")
    void deliveryAddressRoundTrip() {
        MemberDeliveryAddress original = MemberDeliveryAddress.reconstitute(
            91L, MemberId.of(92L), "집", "도로명 주소", "지번 주소", "상세 주소", AdminDongId.of(93L),
            new BigDecimal("37.123456"), new BigDecimal("127.654321"), true,
            LocalDateTime.of(2026, 5, 5, 5, 5),
            LocalDateTime.of(2026, 6, 6, 6, 6));

        MemberDeliveryAddress restored = MemberDeliveryAddressStateMapper.toDomain(
            MemberDeliveryAddressStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("MemberSocialAccount → MemberSocialAccountState → MemberSocialAccount 왕복 시 모든 필드가 보존된다")
    void socialAccountRoundTrip() {
        MemberSocialAccount original = MemberSocialAccount.reconstitute(
            101L, MemberId.of(102L), MemberSocialProvider.NAVER, "provider-id", "p@mail.com", "p-nick",
            "https://img.example/p.png",
            LocalDateTime.of(2026, 7, 7, 7, 7),
            LocalDateTime.of(2026, 8, 8, 8, 8),
            LocalDateTime.of(2026, 9, 9, 9, 9));

        MemberSocialAccount restored = MemberSocialAccountStateMapper.toDomain(
            MemberSocialAccountStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("MemberWithdrawal → MemberWithdrawalState → MemberWithdrawal 왕복 시 모든 필드가 보존된다")
    void withdrawalRoundTrip() {
        MemberWithdrawal original = MemberWithdrawal.reconstitute(
            111L, MemberId.of(112L), MemberWithdrawalReason.PRIVACY_CONCERNS, "상세 사유",
            LocalDateTime.of(2026, 10, 10, 10, 10),
            LocalDateTime.of(2026, 11, 11, 11, 11));

        MemberWithdrawal restored = MemberWithdrawalStateMapper.toDomain(MemberWithdrawalStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
