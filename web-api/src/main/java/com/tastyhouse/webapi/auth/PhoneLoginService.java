package com.tastyhouse.webapi.auth;

import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.entity.user.MemberStatus;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.MemberCoreService;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.auth.response.PhoneLoginResponse;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhoneLoginService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberCoreService memberCoreService;
    private final TokenService tokenService;

    // phoneVerifyToken을 검증하고, 해당 번호로 가입된 회원이 있으면 JWT 발급
    // 없으면 needsSignUp=true 반환
    @Transactional(readOnly = true)
    public PhoneLoginResponse login(String phoneVerifyToken) {
        if (!jwtTokenProvider.validatePhoneVerifyToken(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromPhoneVerifyToken(phoneVerifyToken);

        Optional<Member> memberOpt = memberCoreService.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        if (memberOpt.isPresent()) {
            JwtResponse jwt = issueJwt(memberOpt.get().getUsername());
            return PhoneLoginResponse.ofLogin(jwt);
        }

        return PhoneLoginResponse.ofSignUpRequired();
    }

    private JwtResponse issueJwt(String username) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            username, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return tokenService.issue(authentication, false);
    }
}
