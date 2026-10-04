package com.tastyhouse.application.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.auth.token.MemberJwtTokenProvider;
import com.tastyhouse.application.auth.token.MemberTokenService;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
public class MemberAuthService {

    private final MemberPersistencePort memberPersistencePort;
    private final PasswordEncoder passwordEncoder;
    private final MemberJwtTokenProvider jwtTokenProvider;
    private final MemberTokenService tokenService;

    public MemberAuthService(
        MemberPersistencePort memberPersistencePort,
        PasswordEncoder passwordEncoder,
        MemberJwtTokenProvider jwtTokenProvider,
        MemberTokenService tokenService
    ) {
        this.memberPersistencePort = memberPersistencePort;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public void verifyPassword(Long memberId, String rawPassword) {
        Member member = memberPersistencePort.findById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new ApplicationException(WebErrorCode.MEMBER_PASSWORD_MISMATCH);
        }
    }

    public void verifyPersonalInfoToken(Long memberId, String verifyToken) {
        if (!jwtTokenProvider.validateVerifyToken(verifyToken)) {
            throw new ApplicationException(WebErrorCode.MEMBER_INFO_AUTH_EXPIRED);
        }

        Long verifiedMemberId = jwtTokenProvider.getMemberIdFromVerifyToken(verifyToken);
        if (!verifiedMemberId.equals(memberId)) {
            throw new ApplicationException(WebErrorCode.AUTH_VERIFICATION_MISMATCH);
        }
    }

    public void verifyPhoneToken(Long memberId, String smsVerifyToken, String phoneNumber) {
        if (!StringUtils.hasText(smsVerifyToken)) {
            throw new ApplicationException(WebErrorCode.MEMBER_PHONE_SMS_REQUIRED);
        }

        if (jwtTokenProvider.isInvalidSmsVerifyToken(smsVerifyToken)) {
            throw new ApplicationException(WebErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        Long phoneVerifiedMemberId = jwtTokenProvider.getMemberIdFromSmsVerifyToken(smsVerifyToken);
        if (!phoneVerifiedMemberId.equals(memberId)) {
            throw new ApplicationException(WebErrorCode.AUTH_PHONE_VERIFICATION_MISMATCH);
        }

        String verifiedPhoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        if (!verifiedPhoneNumber.equals(phoneNumber)) {
            throw new ApplicationException(WebErrorCode.MEMBER_PHONE_MISMATCH);
        }
    }

    public void verifySignUpTokens(String phoneNumber, String smsVerifyToken,
                                   String username, String mailVerifyToken) {
        if (!StringUtils.hasText(smsVerifyToken) || jwtTokenProvider.isInvalidSmsVerifyToken(smsVerifyToken)) {
            throw new ApplicationException(WebErrorCode.MEMBER_SIGNUP_PHONE_REQUIRED);
        }

        String verifiedPhone = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        if (!verifiedPhone.equals(phoneNumber)) {
            throw new ApplicationException(WebErrorCode.MEMBER_PHONE_MISMATCH);
        }

        if (!StringUtils.hasText(mailVerifyToken)) {
            throw new ApplicationException(WebErrorCode.MEMBER_SIGNUP_EMAIL_REQUIRED);
        }

        if (!jwtTokenProvider.validateMailVerifyToken(mailVerifyToken)) {
            throw new ApplicationException(WebErrorCode.MEMBER_EMAIL_AUTH_EXPIRED);
        }

        String verifiedEmail = jwtTokenProvider.getEmailFromMailVerifyToken(mailVerifyToken);
        if (!verifiedEmail.equals(username)) {
            throw new ApplicationException(WebErrorCode.MEMBER_EMAIL_MISMATCH);
        }
    }

    public String createPersonalInfoVerifyToken(Long memberId) {
        return jwtTokenProvider.createPersonalInfoVerifyToken(memberId);
    }

    public void invalidateAccessToken(String bearerToken) {
        tokenService.invalidateAccessToken(bearerToken);
    }
}
