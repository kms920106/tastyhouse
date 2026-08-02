package com.tastyhouse.webapi.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.member.domain.model.Member;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.service.TokenService;

@Service
public class MemberAuthService {

    private final MemberQueryService memberQueryService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    public MemberAuthService(
        MemberQueryService memberQueryService,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider,
        TokenService tokenService
    ) {
        this.memberQueryService = memberQueryService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenService = tokenService;
    }

    // 입력한 비밀번호가 저장된 비밀번호와 일치하는지 검증
    @Transactional(readOnly = true)
    public void verifyPassword(Long memberId, String rawPassword) {
        Member member = memberQueryService.getMember(memberId);

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

        if (!jwtTokenProvider.validateSmsVerifyToken(smsVerifyToken)) {
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
        if (!StringUtils.hasText(smsVerifyToken) || !jwtTokenProvider.validateSmsVerifyToken(smsVerifyToken)) {
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
