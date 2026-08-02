package com.tastyhouse.webapi.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.member.domain.model.Member;
import com.tastyhouse.domain.member.domain.model.MemberGender;
import com.tastyhouse.domain.member.domain.model.MemberSocialAccount;
import com.tastyhouse.domain.member.domain.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.domain.member.domain.repository.MemberSocialAccountRepository;
import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.member.domain.service.MemberRegistrationService;
import com.tastyhouse.domain.member.domain.service.MemberWithdrawalService;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 회원 명령 서비스.
 *
 * <p>프로필·개인정보·비밀번호 변경은 {@code Member} 애그리거트 하나만 다루는 단일 애그리거트 연산
 * (분류 A)이므로 이 서비스가 직접 write 포트로 처리한다. 가입·탈퇴는 추천 관계·탈퇴 사유 등 다른
 * 애그리거트를 함께 다루는 불변식이므로 도메인 서비스({@link MemberRegistrationService},
 * {@link MemberWithdrawalService})에 위임한다.
 *
 * <p>비밀번호 인코딩은 Spring Security {@code PasswordEncoder}에 의존하므로 프레임워크-프리 도메인
 * 계층이 아니라 이 계층에서 수행하고, 도메인에는 인코딩된 값만 넘긴다.
 *
 * <p>도메인이 프레임워크-프리라 더티 체킹이 없으므로 변경 후 저장은 명시적 save로 수행한다.
 */
@Service
@Transactional
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final MemberRegistrationService memberRegistrationService;
    private final MemberWithdrawalService memberWithdrawalService;
    private final PasswordEncoder passwordEncoder;

    public MemberCommandService(
        MemberRepository memberRepository,
        MemberSocialAccountRepository memberSocialAccountRepository,
        MemberRegistrationService memberRegistrationService,
        MemberWithdrawalService memberWithdrawalService,
        PasswordEncoder passwordEncoder
    ) {
        this.memberRepository = memberRepository;
        this.memberSocialAccountRepository = memberSocialAccountRepository;
        this.memberRegistrationService = memberRegistrationService;
        this.memberWithdrawalService = memberWithdrawalService;
        this.passwordEncoder = passwordEncoder;
    }

    public Long signUp(
        String username,
        String rawPassword,
        String nickname,
        String fullName,
        MemberGender gender,
        Integer birthDate,
        String phoneNumber,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled,
        String referrerNickname
    ) {
        return memberRegistrationService.signUp(
            username,
            passwordEncoder.encode(rawPassword),
            nickname,
            fullName,
            gender,
            birthDate,
            phoneNumber,
            pushNotificationEnabled,
            marketingInfoEnabled,
            eventInfoEnabled,
            referrerNickname
        );
    }

    /** 소셜 가입 — 소셜로그인 서비스(4종)가 신원 확인 후 호출한다. */
    public Member signUpSocial(
        String username,
        String nickname,
        String fullName,
        MemberGender gender,
        Integer birthDate,
        String phoneNumber,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled,
        String referrerNickname
    ) {
        return memberRegistrationService.signUpSocial(
            username,
            nickname,
            fullName,
            gender,
            birthDate,
            phoneNumber,
            pushNotificationEnabled,
            marketingInfoEnabled,
            eventInfoEnabled,
            referrerNickname
        );
    }

    public void updateProfile(Long memberId, String nickname, String statusMessage, Long profileImageFileId) {
        Member member = loadMember(memberId);
        UploadedFileId uploadedFileId = profileImageFileId == null ? null : UploadedFileId.of(profileImageFileId);
        member.updateProfile(nickname, statusMessage, uploadedFileId);
        memberRepository.save(member);
    }

    public void updatePersonalInfo(
        Long memberId,
        String fullName,
        String phoneNumber,
        Integer birthDate,
        MemberGender gender,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled
    ) {
        Member member = loadMember(memberId);
        member.updatePersonalInfo(
            fullName, phoneNumber, birthDate, gender,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled
        );
        memberRepository.save(member);
    }

    /**
     * 새 비밀번호와 확인값 일치를 검증한 뒤 인코딩해 변경한다.
     *
     * <p><b>트랜잭션 원자성 판정</b> — "기존 비밀번호와 동일한지" 검증은 <em>DB에서 읽은 현재 비밀번호</em>에
     * 의존하는 read-then-write이므로, 검증과 변경이 같은 트랜잭션·같은 회원 로드 안에서 일어나야 한다.
     * 과거에는 이 검증이 파사드({@code MemberService.updatePassword})에서 별도 readOnly 트랜잭션
     * ({@code MemberAuthService.verifyNotSamePassword})으로 수행돼, 검증 후 변경 사이에 비밀번호가 바뀌면
     * 검사를 우회할 수 있고 회원을 두 번 로드하는 구조였다. 검증을 이 메서드 안으로 내려 단일 트랜잭션·
     * 단일 로드로 원자화했다.
     *
     * <p>기존 예외 계약·검사 순서를 그대로 보존한다 — 파사드는 {@code verifyNotSamePassword}(동일 여부)를
     * 먼저 호출하고 그 다음 이 메서드의 확인값 검사가 돌았으므로, 여기서도 <b>동일 여부
     * ({@code MEMBER_PASSWORD_SAME_AS_OLD}) → 확인값 불일치({@code MEMBER_PASSWORD_CONFIRM_MISMATCH})</b>
     * 순서를 유지한다. 순서를 뒤집으면 두 조건을 동시에 위반한 요청의 응답 코드가 바뀐다.
     */
    public void updatePassword(Long memberId, String newPassword, String newPasswordConfirm) {
        Member member = loadMember(memberId);

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_SAME_AS_OLD);
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_CONFIRM_MISMATCH);
        }

        member.updatePassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    public void withdraw(Long memberId, MemberWithdrawalReason reason, String reasonDetail) {
        memberWithdrawalService.withdraw(MemberId.of(memberId), reason, reasonDetail);
    }

    /** 소셜 계정 신규 저장·제공자 정보 갱신을 반영한다. */
    public void saveSocialAccount(MemberSocialAccount socialAccount) {
        memberSocialAccountRepository.save(socialAccount);
    }

    private Member loadMember(Long memberId) {
        return memberRepository.findById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
