package com.tastyhouse.application.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;
import com.tastyhouse.application.auth.port.out.PhoneLoginResult;
import com.tastyhouse.application.auth.token.MemberJwtTokenProvider;
import com.tastyhouse.application.auth.token.MemberTokenService;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.shared.marker.WebApp;

@Service
@WebApp
public class PhoneLoginService {

    private final MemberJwtTokenProvider jwtTokenProvider;
    private final MemberPersistencePort memberPersistencePort;
    private final MemberTokenService tokenService;

    public PhoneLoginService(
        MemberJwtTokenProvider jwtTokenProvider,
        MemberPersistencePort memberPersistencePort,
        MemberTokenService tokenService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberPersistencePort = memberPersistencePort;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public PhoneLoginResult login(String smsVerifyToken) {
        if (jwtTokenProvider.isInvalidSmsVerifyToken(smsVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);

        Optional<Member> memberOpt = memberPersistencePort.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        if (memberOpt.isPresent()) {
            MemberJwtResult jwt = tokenService.issue(memberOpt.get(), false);
            return PhoneLoginResult.ofLogin(jwt);
        }

        return PhoneLoginResult.ofSignUpRequired();
    }
}
