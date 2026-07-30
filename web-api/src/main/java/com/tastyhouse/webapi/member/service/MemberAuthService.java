package com.tastyhouse.webapi.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import com.tastyhouse.webapi.exception.UnauthorizedException;

@Service
@RequiredArgsConstructor
public class MemberAuthService {

    private final MemberQueryService memberQueryService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

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
            throw new UnauthorizedException("인증 정보가 일치하지 않습니다.");
        }
    }

    // 휴대폰 인증 토큰의 유효성과 회원·번호 일치 여부를 검증
    public void verifyPhoneToken(Long memberId, String phoneVerifyToken, String phoneNumber) {
        if (!StringUtils.hasText(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_SMS_REQUIRED);
        }

        if (!jwtTokenProvider.validatePhoneVerifyToken(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        Long phoneVerifiedMemberId = jwtTokenProvider.getMemberIdFromPhoneVerifyToken(phoneVerifyToken);
        if (!phoneVerifiedMemberId.equals(memberId)) {
            throw new UnauthorizedException("휴대폰 인증 정보가 일치하지 않습니다.");
        }

        String verifiedPhoneNumber = jwtTokenProvider.getPhoneNumberFromPhoneVerifyToken(phoneVerifyToken);
        if (!verifiedPhoneNumber.equals(phoneNumber)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_MISMATCH);
        }
    }

    // 회원가입 시 휴대폰·이메일 인증 토큰 유효성과 일치 여부를 검증
    public void verifySignUpTokens(String phoneNumber, String phoneVerifyToken,
                                   String username, String emailVerifyToken) {
        if (!StringUtils.hasText(phoneVerifyToken) || !jwtTokenProvider.validatePhoneVerifyToken(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_SIGNUP_PHONE_REQUIRED);
        }

        String verifiedPhone = jwtTokenProvider.getPhoneNumberFromPhoneVerifyToken(phoneVerifyToken);
        if (!verifiedPhone.equals(phoneNumber)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_MISMATCH);
        }

        if (!StringUtils.hasText(emailVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_SIGNUP_EMAIL_REQUIRED);
        }

        if (!jwtTokenProvider.validateEmailVerifyToken(emailVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_AUTH_EXPIRED);
        }

        String verifiedEmail = jwtTokenProvider.getEmailFromEmailVerifyToken(emailVerifyToken);
        if (!verifiedEmail.equals(username)) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_MISMATCH);
        }
    }

    // 새 비밀번호가 기존 비밀번호와 동일한 경우 예외 처리
    @Transactional(readOnly = true)
    public void verifyNotSamePassword(Long memberId, String newPassword) {
        Member member = memberQueryService.getMember(memberId);
        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_SAME_AS_OLD);
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
