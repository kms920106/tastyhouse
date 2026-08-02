package com.tastyhouse.webapi.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import com.tastyhouse.webapi.auth.response.AuthJwtResponse;
import com.tastyhouse.webapi.auth.response.AuthPhoneLoginResponse;

@Service
public class PhoneLoginService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final TokenService tokenService;

    public PhoneLoginService(
        JwtTokenProvider jwtTokenProvider,
        MemberRepository memberRepository,
        TokenService tokenService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
        this.tokenService = tokenService;
    }

    // smsVerifyToken을 검증하고, 해당 번호로 가입된 회원이 있으면 JWT 발급
    // 없으면 needsSignUp=true 반환
    @Transactional(readOnly = true)
    public AuthPhoneLoginResponse login(String smsVerifyToken) {
        if (!jwtTokenProvider.validateSmsVerifyToken(smsVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);

        Optional<Member> memberOpt = memberRepository.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        if (memberOpt.isPresent()) {
            AuthJwtResponse jwt = tokenService.issue(memberOpt.get(), false);
            return AuthPhoneLoginResponse.ofLogin(jwt);
        }

        return AuthPhoneLoginResponse.ofSignUpRequired();
    }
}
