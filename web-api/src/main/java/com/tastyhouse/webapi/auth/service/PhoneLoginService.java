package com.tastyhouse.webapi.auth.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.auth.response.PhoneLoginResponse;

@Service
@RequiredArgsConstructor
public class PhoneLoginService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberQueryService memberQueryService;
    private final TokenService tokenService;

    // phoneVerifyToken을 검증하고, 해당 번호로 가입된 회원이 있으면 JWT 발급
    // 없으면 needsSignUp=true 반환
    @Transactional(readOnly = true)
    public PhoneLoginResponse login(String phoneVerifyToken) {
        if (!jwtTokenProvider.validatePhoneVerifyToken(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromPhoneVerifyToken(phoneVerifyToken);

        Optional<Member> memberOpt = memberQueryService.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        if (memberOpt.isPresent()) {
            JwtResponse jwt = tokenService.issue(memberOpt.get(), false);
            return PhoneLoginResponse.ofLogin(jwt);
        }

        return PhoneLoginResponse.ofSignUpRequired();
    }
}
