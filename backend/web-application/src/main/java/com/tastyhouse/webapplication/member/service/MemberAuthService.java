package com.tastyhouse.webapplication.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.webapplication.auth.token.JwtTokenProvider;
import com.tastyhouse.webapplication.auth.token.TokenService;

/**
 * 회원 인증 협력 서비스.
 *
 * <p>비밀번호 검증은 도메인 모델({@code Member#getPassword})이 있어야 하므로 write 포트
 * ({@link MemberRepository})로 애그리거트를 직접 로드한다 — 표현용 투영이 아니라 도메인 상태 검증이라
 * 읽기 포트의 몫이 아니다. 과거에는 {@code MemberQueryService#getMember}를 빌려 썼는데, 그 탓에
 * {@code *QueryService}가 write 포트를 들고 있어야 했고 CQRS 교차 주입 금지 규칙의 예외로 남아 있었다.
 */
@Service
public class MemberAuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    public MemberAuthService(
        MemberRepository memberRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider,
        TokenService tokenService
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenService = tokenService;
    }

    // 입력한 비밀번호가 저장된 비밀번호와 일치하는지 검증
    @Transactional(readOnly = true)
    public void verifyPassword(Long memberId, String rawPassword) {
        Member member = memberRepository.findById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_MISMATCH);
        }
    }

    // 개인정보 수정용 본인인증 토큰의 유효성과 회원 일치 여부를 검증
    public void verifyPersonalInfoToken(Long memberId, String verifyToken) {
        if (!jwtTokenProvider.validateVerifyToken(verifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_INFO_AUTH_EXPIRED);
        }

        Long verifiedMemberId = jwtTokenProvider.getMemberIdFromVerifyToken(verifyToken);
        if (!verifiedMemberId.equals(memberId)) {
            throw new BusinessException(ErrorCode.AUTH_VERIFICATION_MISMATCH);
        }
    }

    // 휴대폰 인증 토큰의 유효성과 회원·번호 일치 여부를 검증
    public void verifyPhoneToken(Long memberId, String smsVerifyToken, String phoneNumber) {
        if (!StringUtils.hasText(smsVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_SMS_REQUIRED);
        }

        if (jwtTokenProvider.isInvalidSmsVerifyToken(smsVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        Long phoneVerifiedMemberId = jwtTokenProvider.getMemberIdFromSmsVerifyToken(smsVerifyToken);
        if (!phoneVerifiedMemberId.equals(memberId)) {
            throw new BusinessException(ErrorCode.AUTH_PHONE_VERIFICATION_MISMATCH);
        }

        String verifiedPhoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        if (!verifiedPhoneNumber.equals(phoneNumber)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_MISMATCH);
        }
    }

    // 회원가입 시 휴대폰·이메일 인증 토큰 유효성과 일치 여부를 검증
    public void verifySignUpTokens(String phoneNumber, String smsVerifyToken,
                                   String username, String mailVerifyToken) {
        if (!StringUtils.hasText(smsVerifyToken) || jwtTokenProvider.isInvalidSmsVerifyToken(smsVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_SIGNUP_PHONE_REQUIRED);
        }

        String verifiedPhone = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        if (!verifiedPhone.equals(phoneNumber)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_MISMATCH);
        }

        if (!StringUtils.hasText(mailVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_SIGNUP_EMAIL_REQUIRED);
        }

        if (!jwtTokenProvider.validateMailVerifyToken(mailVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_AUTH_EXPIRED);
        }

        String verifiedEmail = jwtTokenProvider.getEmailFromMailVerifyToken(mailVerifyToken);
        if (!verifiedEmail.equals(username)) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_MISMATCH);
        }
    }

    // 개인정보 수정용 본인인증 토큰 생성
    public String createPersonalInfoVerifyToken(Long memberId) {
        return jwtTokenProvider.createPersonalInfoVerifyToken(memberId);
    }

    // 액세스 토큰 무효화
    public void invalidateAccessToken(String bearerToken) {
        tokenService.invalidateAccessToken(bearerToken);
    }
}
